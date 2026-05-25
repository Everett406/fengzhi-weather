package com.fengzhi.weather.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.fengzhi.weather.data.api.NictApi
import com.fengzhi.weather.data.model.AnimationSpeed
import com.fengzhi.weather.data.model.ParsedTimestamp
import com.fengzhi.weather.data.model.SatelliteBand
import com.fengzhi.weather.data.model.SatelliteImage
import com.fengzhi.weather.data.model.SatelliteTile
import com.fengzhi.weather.data.model.SatelliteTimestamp
import com.fengzhi.weather.data.model.SatelliteUiState
import com.fengzhi.weather.data.model.TimelinePoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 卫星云图数据仓库
 * 负责从 NICT API 获取卫星图像数据并进行缓存管理
 */
@Singleton
class SatelliteRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val nictApi: NictApi
) {
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val cacheDir = File(context.cacheDir, "satellite_cache").apply {
        if (!exists()) mkdirs()
    }

    /**
     * 获取最新的卫星图像时间戳
     */
    suspend fun getLatestTimestamp(): Result<SatelliteTimestamp> = withContext(Dispatchers.IO) {
        try {
            val response = nictApi.getLatestTimestamp()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("获取时间戳失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 生成过去24小时的时间轴点
     * Himawari-8 每10分钟更新一次
     */
    fun generateTimelinePoints(latestTimestamp: ParsedTimestamp): List<TimelinePoint> {
        val points = mutableListOf<TimelinePoint>()
        val calendar = Calendar.getInstance()
        
        // 解析最新时间戳
        calendar.set(
            latestTimestamp.year.toInt(),
            latestTimestamp.month.toInt() - 1,
            latestTimestamp.day.toInt(),
            latestTimestamp.hour.toInt(),
            latestTimestamp.minute.toInt(),
            0
        )

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val displayFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())

        // 生成过去24小时的时间点 (每10分钟一个点，共144个点)
        for (i in 0 until 144) {
            val timeInMillis = calendar.timeInMillis
            val hour = String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY))
            val minute = String.format("%02d", calendar.get(Calendar.MINUTE))
            
            val parsedTimestamp = ParsedTimestamp(
                year = String.format("%04d", calendar.get(Calendar.YEAR)),
                month = String.format("%02d", calendar.get(Calendar.MONTH) + 1),
                day = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH)),
                hour = hour,
                minute = minute,
                hourMinute = "$hour$minute"
            )

            points.add(
                TimelinePoint(
                    timestamp = parsedTimestamp,
                    displayTime = displayFormat.format(calendar.time),
                    millis = timeInMillis,
                    isSelected = i == 0
                )
            )

            // 回退10分钟
            calendar.add(Calendar.MINUTE, -10)
        }

        return points.reversed() // 从旧到新排序
    }

    /**
     * 获取卫星图像数据
     */
    suspend fun getSatelliteImage(
        timestamp: ParsedTimestamp,
        band: SatelliteBand,
        scale: Int = NictApi.DEFAULT_SCALE
    ): Result<SatelliteImage> = withContext(Dispatchers.IO) {
        try {
            val tiles = mutableListOf<SatelliteTile>()
            
            for (x in 0 until scale) {
                for (y in 0 until scale) {
                    val url = when (band) {
                        SatelliteBand.VISIBLE_LIGHT -> NictApi.getVisibleLightTileUrl(
                            scale, timestamp.year, timestamp.month, timestamp.day,
                            timestamp.hourMinute, x, y
                        )
                        SatelliteBand.INFRARED -> NictApi.getInfraredTileUrl(
                            scale, timestamp.year, timestamp.month, timestamp.day,
                            timestamp.hourMinute, x, y
                        )
                        SatelliteBand.WATER_VAPOR -> NictApi.getWaterVaporTileUrl(
                            scale, timestamp.year, timestamp.month, timestamp.day,
                            timestamp.hourMinute, x, y
                        )
                    }
                    
                    tiles.add(
                        SatelliteTile(
                            x = x,
                            y = y,
                            url = url,
                            band = band,
                            timestamp = timestamp
                        )
                    )
                }
            }

            Result.success(
                SatelliteImage(
                    timestamp = timestamp,
                    band = band,
                    scale = scale,
                    tiles = tiles
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 下载单个瓦片图像
     */
    suspend fun downloadTile(url: String): Result<Bitmap> = withContext(Dispatchers.IO) {
        try {
            // 先检查缓存
            val cacheFile = getCacheFile(url)
            if (cacheFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(cacheFile.absolutePath)
                if (bitmap != null) {
                    return@withContext Result.success(bitmap)
                }
            }

            // 下载图像
            val request = Request.Builder()
                .url(url)
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("下载失败: ${response.code}"))
            }

            val inputStream = response.body?.byteStream()
                ?: return@withContext Result.failure(Exception("响应体为空"))

            val bitmap = BitmapFactory.decodeStream(inputStream)
                ?: return@withContext Result.failure(Exception("解码图像失败"))

            // 保存到缓存
            saveToCache(cacheFile, bitmap)

            Result.success(bitmap)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 合并瓦片为完整图像
     */
    suspend fun composeTiles(
        tiles: List<Bitmap>,
        scale: Int
    ): Result<Bitmap> = withContext(Dispatchers.IO) {
        try {
            if (tiles.isEmpty()) {
                return@withContext Result.failure(Exception("没有瓦片可合并"))
            }

            val tileSize = tiles[0].width
            val totalSize = tileSize * scale

            val composedBitmap = Bitmap.createBitmap(
                totalSize, totalSize, Bitmap.Config.ARGB_8888
            )

            val canvas = android.graphics.Canvas(composedBitmap)

            tiles.forEachIndexed { index, tile ->
                val x = index % scale
                val y = index / scale
                canvas.drawBitmap(tile, (x * tileSize).toFloat(), (y * tileSize).toFloat(), null)
            }

            Result.success(composedBitmap)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取缓存文件
     */
    private fun getCacheFile(url: String): File {
        val fileName = url.hashCode().toString()
        return File(cacheDir, fileName)
    }

    /**
     * 保存到缓存
     */
    private fun saveToCache(file: File, bitmap: Bitmap) {
        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
        } catch (e: Exception) {
            // 缓存失败不影响主流程
        }
    }

    /**
     * 清除缓存
     */
    fun clearCache() {
        cacheDir.listFiles()?.forEach { it.delete() }
    }

    /**
     * 获取缓存大小
     */
    fun getCacheSize(): Long {
        return cacheDir.listFiles()?.sumOf { it.length() } ?: 0L
    }

    /**
     * 格式化缓存大小显示
     */
    fun formatCacheSize(): String {
        val size = getCacheSize()
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> String.format("%.1f KB", size / 1024.0)
            else -> String.format("%.1f MB", size / (1024.0 * 1024.0))
        }
    }
}
