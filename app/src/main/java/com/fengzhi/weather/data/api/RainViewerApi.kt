package com.fengzhi.weather.data.api

import com.fengzhi.weather.data.model.RainViewerMetadata
import retrofit2.Response
import retrofit2.http.GET

/**
 * RainViewer API 接口
 * 提供全球降水雷达数据
 */
interface RainViewerApi {
    
    /**
     * 获取雷达元数据
     * 包含雷达帧列表、服务器地址等信息
     */
    @GET("public/weather-maps.json")
    suspend fun getMetadata(): Response<RainViewerMetadata>
}
