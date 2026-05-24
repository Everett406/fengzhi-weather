package com.fengzhi.weather.data.model

import com.google.gson.annotations.SerializedName

/**
 * 城市数据模型
 * 用于存储用户保存的城市列表
 */
data class City(
    val id: String,              // 城市唯一标识（和风天气 location id）
    val name: String,            // 城市名称
    val adm2: String? = null,    // 上级行政区划（如：北京市、广东省）
    val adm1: String? = null,    // 一级行政区划（如：北京市、广东省）
    val country: String? = null, // 国家
    val latitude: Double,        // 纬度
    val longitude: Double,       // 经度
    val isCurrent: Boolean = false // 是否为当前选中城市
) {
    /**
     * 获取完整显示名称
     * 格式：城市名, 上级行政区
     */
    val displayName: String
        get() = when {
            adm2 != null && adm2 != name -> "$name, $adm2"
            adm1 != null && adm1 != name -> "$name, $adm1"
            else -> name
        }
    
    /**
     * 获取简短显示名称
     */
    val shortName: String
        get() = name

    /**
     * 获取位置字符串（经度,纬度）用于 API 请求
     */
    val locationString: String
        get() = "$longitude,$latitude"
}

/**
 * 城市搜索结果
 */
data class CitySearchResult(
    val cities: List<City>,
    val query: String
)

/**
 * 和风天气城市查询响应
 */
data class QWeatherCityResponse(
    @SerializedName("code")
    val code: String,
    @SerializedName("location")
    val location: List<QWeatherCityLocation>?
)

/**
 * 和风天气城市位置信息
 */
data class QWeatherCityLocation(
    @SerializedName("name")
    val name: String,
    @SerializedName("id")
    val id: String,
    @SerializedName("lat")
    val lat: String,
    @SerializedName("lon")
    val lon: String,
    @SerializedName("adm2")
    val adm2: String?,
    @SerializedName("adm1")
    val adm1: String?,
    @SerializedName("country")
    val country: String?
) {
    /**
     * 转换为 City 模型
     */
    fun toCity(isCurrent: Boolean = false): City {
        return City(
            id = id,
            name = name,
            adm2 = adm2,
            adm1 = adm1,
            country = country,
            latitude = lat.toDoubleOrNull() ?: 0.0,
            longitude = lon.toDoubleOrNull() ?: 0.0,
            isCurrent = isCurrent
        )
    }
}
