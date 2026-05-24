package com.fengzhi.weather.data.repository

import android.util.Log
import com.fengzhi.weather.data.api.RainViewerApi
import com.fengzhi.weather.data.model.RadarFrame
import com.fengzhi.weather.data.model.RadarMapConfig
import com.fengzhi.weather.data.model.RadarState
import com.fengzhi.weather.data.model.RainViewerMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 雷达数据仓库
 * 负责从 RainViewer API 获取降水雷达数据
 */
@Singleton
class RadarRepository @Inject constructor(
    private val rainViewerApi: RainViewerApi
) {
    companion object {
        private const val TAG = "RadarRepository"
    }
    
    /**
     * 获取雷达元数据和帧列表
     */
    suspend fun getRadarData(): RadarState = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "开始获取雷达数据")
            val response = rainViewerApi.getMetadata()
            
            if (response.isSuccessful && response.body() != null) {
                val metadata = response.body()!!
                Log.d(TAG, "成功获取雷达数据，版本: ${metadata.version}, 主机: ${metadata.host}")
                
                // 合并过去和预报帧
                val allFrames = mutableListOf<RadarFrame>()
                allFrames.addAll(metadata.radar.past)
                allFrames.addAll(metadata.radar.nowcast)
                
                // 按时间排序
                allFrames.sortBy { it.time }
                
                Log.d(TAG, "总帧数: ${allFrames.size}, 过去: ${metadata.radar.past.size}, 预报: ${metadata.radar.nowcast.size}")
                
                RadarState.Success(
                    metadata = metadata,
                    allFrames = allFrames,
                    currentFrameIndex = allFrames.indexOfLast { !it.isNowcast() }.coerceAtLeast(0),
                    centerLat = RadarMapConfig.DEFAULT_CENTER_LAT,
                    centerLon = RadarMapConfig.DEFAULT_CENTER_LON,
                    zoom = RadarMapConfig.DEFAULT_ZOOM
                )
            } else {
                Log.e(TAG, "获取雷达数据失败: ${response.code()} - ${response.message()}")
                RadarState.Error("获取雷达数据失败: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "获取雷达数据异常", e)
            RadarState.Error("网络错误: ${e.message ?: "未知错误"}")
        }
    }
    
    /**
     * 获取指定区域的瓦片URL列表
     * @param metadata 雷达元数据
     * @param frame 雷达帧
     * @param centerLat 中心纬度
     * @param centerLon 中心经度
     * @param zoom 缩放级别
     * @param viewWidth 视图宽度（像素）
     * @param viewHeight 视图高度（像素）
     */
    fun getTileUrls(
        metadata: RainViewerMetadata,
        frame: RadarFrame,
        centerLat: Double,
        centerLon: Double,
        zoom: Int,
        viewWidth: Int,
        viewHeight: Int
    ): List<String> {
        val tileSize = RadarMapConfig.TILE_SIZE
        val tilesX = (viewWidth / tileSize) + 2
        val tilesY = (viewHeight / tileSize) + 2
        
        // 计算中心瓦片坐标
        val centerTileX = lonToTileX(centerLon, zoom)
        val centerTileY = latToTileY(centerLat, zoom)
        
        val urls = mutableListOf<String>()
        
        // 生成瓦片URL
        for (dx in -tilesX / 2..tilesX / 2) {
            for (dy in -tilesY / 2..tilesY / 2) {
                val tileX = centerTileX + dx
                val tileY = centerTileY + dy
                
                // 检查瓦片坐标是否有效
                if (isValidTile(tileX, tileY, zoom)) {
                    urls.add(frame.getTileUrl(metadata.host, zoom, tileX, tileY))
                }
            }
        }
        
        return urls
    }
    
    /**
     * 经度转瓦片X坐标
     */
    private fun lonToTileX(lon: Double, zoom: Int): Int {
        return ((lon + 180) / 360 * (1 shl zoom)).toInt()
    }
    
    /**
     * 纬度转瓦片Y坐标
     */
    private fun latToTileY(lat: Double, zoom: Int): Int {
        val latRad = Math.toRadians(lat)
        return ((1 - Math.log(Math.tan(latRad) + 1 / Math.cos(latRad)) / Math.PI) / 2 * (1 shl zoom)).toInt()
    }
    
    /**
     * 检查瓦片坐标是否有效
     */
    private fun isValidTile(x: Int, y: Int, zoom: Int): Boolean {
        val max = 1 shl zoom
        return x in 0 until max && y in 0 until max
    }
    
    /**
     * 获取单个瓦片URL（简化版本，用于静态显示）
     */
    fun getSingleTileUrl(
        metadata: RainViewerMetadata,
        frame: RadarFrame,
        zoom: Int = RadarMapConfig.DEFAULT_ZOOM
    ): String {
        val centerTileX = lonToTileX(RadarMapConfig.DEFAULT_CENTER_LON, zoom)
        val centerTileY = latToTileY(RadarMapConfig.DEFAULT_CENTER_LAT, zoom)
        return frame.getTileUrl(metadata.host, zoom, centerTileX, centerTileY)
    }
}
