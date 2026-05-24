package com.fengzhi.weather.data.model

import com.google.gson.annotations.SerializedName

/**
 * 卫星图像时间戳响应
 */
data class SatelliteTimestamp(
    @SerializedName("date")
    val date: String,  // 格式: "2024-01-15 08:00:00"
    
    @SerializedName("file")
    val file: String   // 格式: "20240115080000"
) {
    /**
     * 解析时间戳为各个部分
     * @return ParsedTimestamp 或 null 如果解析失败
     */
    fun parseTimestamp(): ParsedTimestamp? {
        return try {
            // file 格式: "20240115080000"
            val year = file.substring(0, 4)
            val month = file.substring(4, 6)
            val day = file.substring(6, 8)
            val hour = file.substring(8, 10)
            val minute = file.substring(10, 12)
            
            ParsedTimestamp(
                year = year,
                month = month,
                day = day,
                hour = hour,
                minute = minute,
                hourMinute = "$hour$minute"
            )
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * 解析后的时间戳数据
 */
data class ParsedTimestamp(
    val year: String,
    val month: String,
    val day: String,
    val hour: String,
    val minute: String,
    val hourMinute: String
) {
    /**
     * 格式化显示时间
     */
    fun formatDisplay(): String {
        return "$year-$month-$day $hour:$minute"
    }
    
    /**
     * 转换为时间戳 (毫秒)
     */
    fun toMillis(): Long {
        return try {
            val calendar = java.util.Calendar.getInstance()
            calendar.set(
                year.toInt(),
                month.toInt() - 1,
                day.toInt(),
                hour.toInt(),
                minute.toInt(),
                0
            )
            calendar.timeInMillis
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}

/**
 * 卫星图像波段类型
 */
enum class SatelliteBand(val displayName: String, val description: String) {
    VISIBLE_LIGHT("可见光", "真彩色图像，白天可见"),
    INFRARED("红外", "红外图像，全天候可用"),
    WATER_VAPOR("水汽", "水汽图像，显示大气水汽分布")
}

/**
 * 卫星图像瓦片信息
 */
data class SatelliteTile(
    val x: Int,
    val y: Int,
    val url: String,
    val band: SatelliteBand,
    val timestamp: ParsedTimestamp
)

/**
 * 完整的卫星图像数据
 */
data class SatelliteImage(
    val timestamp: ParsedTimestamp,
    val band: SatelliteBand,
    val scale: Int,
    val tiles: List<SatelliteTile>,
    val composedImageUrl: String? = null
) {
    /**
     * 获取瓦片总数
     */
    val totalTiles: Int
        get() = scale * scale
}

/**
 * 时间轴上的时间点
 */
data class TimelinePoint(
    val timestamp: ParsedTimestamp,
    val displayTime: String,
    val millis: Long,
    val isSelected: Boolean = false
)

/**
 * 卫星图像状态
 */
sealed class SatelliteUiState {
    data object Loading : SatelliteUiState()
    data class Success(
        val currentImage: SatelliteImage,
        val timelinePoints: List<TimelinePoint>,
        val currentBand: SatelliteBand,
        val isAnimating: Boolean = false,
        val animationSpeed: AnimationSpeed = AnimationSpeed.NORMAL
    ) : SatelliteUiState()
    data class Error(val message: String) : SatelliteUiState()
}

/**
 * 动画播放速度
 */
enum class AnimationSpeed(val delayMs: Long, val displayName: String) {
    SLOW(1000L, "慢速"),
    NORMAL(500L, "正常"),
    FAST(200L, "快速")
}

/**
 * 瓦片加载状态
 */
data class TileLoadState(
    val x: Int,
    val y: Int,
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val retryCount: Int = 0
)

/**
 * 卫星图像缓存键
 */
data class SatelliteCacheKey(
    val timestamp: String,
    val band: SatelliteBand,
    val scale: Int,
    val x: Int,
    val y: Int
) {
    override fun toString(): String {
        return "${timestamp}_${band.name}_${scale}_${x}_$y"
    }
}
