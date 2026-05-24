package com.fengzhi.weather.data.model

import com.google.gson.annotations.SerializedName

/**
 * RainViewer API 响应数据模型
 */
data class RainViewerMetadata(
    val version: String,
    val generated: Long,
    val host: String,
    val radar: RadarData,
    val satellite: SatelliteData? = null
)

data class RadarData(
    val past: List<RadarFrame>,
    val nowcast: List<RadarFrame> = emptyList()
)

data class SatelliteData(
    val infrared: List<SatelliteFrame> = emptyList(),
    val visible: List<SatelliteFrame> = emptyList()
)

data class RadarFrame(
    val time: Long,
    val path: String
) {
    /**
     * 获取完整的瓦片URL
     * @param host RainViewer服务器主机地址
     * @param z 缩放级别
     * @param x 瓦片X坐标
     * @param y 瓦片Y坐标
     * @param options 渲染选项 (默认: 2/1_1.png 表示标准颜色和透明度)
     */
    fun getTileUrl(host: String, z: Int, x: Int, y: Int, options: String = "2/1_1.png"): String {
        return "$host$path/256/$z/$x/$y/$options"
    }
    
    /**
     * 获取时间显示字符串
     */
    fun getTimeDisplay(): String {
        val currentTime = System.currentTimeMillis() / 1000
        val diffMinutes = ((currentTime - time) / 60).toInt()
        
        return when {
            diffMinutes < 0 -> "+${-diffMinutes}分钟"
            diffMinutes == 0 -> "现在"
            diffMinutes < 60 -> "${diffMinutes}分钟前"
            else -> "${diffMinutes / 60}小时${diffMinutes % 60}分钟前"
        }
    }
    
    /**
     * 是否为预报数据
     */
    fun isNowcast(): Boolean {
        val currentTime = System.currentTimeMillis() / 1000
        return time > currentTime
    }
}

data class SatelliteFrame(
    val time: Long,
    val path: String
)

/**
 * 雷达图状态
 */
sealed class RadarState {
    data object Loading : RadarState()
    data class Success(
        val metadata: RainViewerMetadata,
        val allFrames: List<RadarFrame>,
        val currentFrameIndex: Int = 0,
        val isPlaying: Boolean = false,
        val centerLat: Double = 35.0,
        val centerLon: Double = 105.0,
        val zoom: Int = 5
    ) : RadarState() {
        val currentFrame: RadarFrame?
            get() = allFrames.getOrNull(currentFrameIndex)
        
        val pastFrames: List<RadarFrame>
            get() = allFrames.filter { !it.isNowcast() }
        
        val nowcastFrames: List<RadarFrame>
            get() = allFrames.filter { it.isNowcast() }
        
        val totalFrames: Int
            get() = allFrames.size
        
        val progress: Float
            get() = if (totalFrames > 0) currentFrameIndex.toFloat() / (totalFrames - 1) else 0f
    }
    data class Error(val message: String) : RadarState()
}

/**
 * 降水强度等级
 */
enum class PrecipitationLevel(
    val displayName: String,
    val colorHex: String,
    val dbzRange: ClosedRange<Int>
) {
    LIGHT("小雨", "#00FF00", 15..20),
    MODERATE("中雨", "#00C800", 20..25),
    HEAVY("大雨", "#01A501", 25..30),
    VERY_HEAVY("暴雨", "#FFFF00", 30..35),
    INTENSE("大暴雨", "#FFE000", 35..40),
    SEVERE("特大暴雨", "#FF9000", 40..45),
    EXTREME("极端降水", "#FF0000", 45..50),
    HAIL("冰雹", "#D70000", 50..100);
    
    companion object {
        fun fromDbz(dbz: Int): PrecipitationLevel? {
            return values().find { dbz in it.dbzRange }
        }
    }
}

/**
 * 雷达瓦片配置
 */
data class RadarTileConfig(
    val frame: RadarFrame,
    val host: String,
    val zoom: Int,
    val x: Int,
    val y: Int
) {
    val url: String
        get() = frame.getTileUrl(host, zoom, x, y)
}

/**
 * 雷达地图配置
 */
object RadarMapConfig {
    // 默认中心位置（中国）
    const val DEFAULT_CENTER_LAT = 35.0
    const val DEFAULT_CENTER_LON = 105.0
    const val DEFAULT_ZOOM = 5
    
    // 瓦片大小
    const val TILE_SIZE = 256
    
    // 动画播放间隔（毫秒）
    const val ANIMATION_INTERVAL = 500L
    
    // RainViewer API
    const val RAINVIEWER_API_URL = "https://api.rainviewer.com/"
    const val RAINVIEWER_METADATA_PATH = "public/weather-maps.json"
    
    // 地图范围（中国区域）
    const val CHINA_MIN_LAT = 18.0
    const val CHINA_MAX_LAT = 54.0
    const val CHINA_MIN_LON = 73.0
    const val CHINA_MAX_LON = 135.0
}
