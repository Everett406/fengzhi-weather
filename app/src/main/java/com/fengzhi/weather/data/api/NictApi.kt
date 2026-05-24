package com.fengzhi.weather.data.api

import com.fengzhi.weather.data.model.SatelliteTimestamp
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * NICT Himawari-8/9 卫星云图 API 接口
 * 用于获取向日葵卫星的实时云图数据
 */
interface NictApi {

    /**
     * 获取最新的卫星图像时间戳
     * 返回格式: {"date":"2024-01-15 08:00:00","file":"20240115080000"}
     */
    @GET("himawari8/img/D531106/latest.json")
    suspend fun getLatestTimestamp(): Response<SatelliteTimestamp>

    /**
     * 获取可见光波段图像瓦片 URL
     * D531106 表示可见光波段
     */
    fun getVisibleLightTileUrl(
        scale: Int,
        year: String,
        month: String,
        day: String,
        hourMinute: String,
        x: Int,
        y: Int
    ): String {
        return "https://himawari8-dl.nict.go.jp/himawari8/img/D531106/${scale}d/550/$year/$month/$day/${hourMinute}00_${x}_$y.png"
    }

    /**
     * 获取红外波段图像瓦片 URL
     * B531106 表示红外波段
     */
    fun getInfraredTileUrl(
        scale: Int,
        year: String,
        month: String,
        day: String,
        hourMinute: String,
        x: Int,
        y: Int
    ): String {
        return "https://himawari8-dl.nict.go.jp/himawari8/img/B531106/${scale}d/550/$year/$month/$day/${hourMinute}00_${x}_$y.png"
    }

    /**
     * 获取水汽波段图像瓦片 URL
     * S531106 表示水汽波段
     */
    fun getWaterVaporTileUrl(
        scale: Int,
        year: String,
        month: String,
        day: String,
        hourMinute: String,
        x: Int,
        y: Int
    ): String {
        return "https://himawari8-dl.nict.go.jp/himawari8/img/S531106/${scale}d/550/$year/$month/$day/${hourMinute}00_${x}_$y.png"
    }

    companion object {
        const val BASE_URL = "https://himawari8-dl.nict.go.jp/"
        
        // 图像缩放级别 (1=1x1, 2=2x2, 4=4x4, 8=8x8, 16=16x16, 20=20x20)
        const val SCALE_1 = 1
        const val SCALE_2 = 2
        const val SCALE_4 = 4
        const val SCALE_8 = 8
        const val SCALE_16 = 16
        const val SCALE_20 = 20
        
        // 默认缩放级别 (2x2 = 4张瓦片)
        const val DEFAULT_SCALE = SCALE_2
        
        // 瓦片尺寸
        const val TILE_SIZE = 550
    }
}
