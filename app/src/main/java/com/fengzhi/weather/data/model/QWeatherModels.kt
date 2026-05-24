package com.fengzhi.weather.data.model

import com.google.gson.annotations.SerializedName

/**
 * 和风天气 API 响应模型
 * API 文档: https://dev.qweather.com/docs/api/
 */

// ============== 通用响应基类 ==============
data class QWeatherBaseResponse(
    val code: String,
    val updateTime: String?,
    val fxLink: String?
) {
    fun isSuccess(): Boolean = code == "200"
}

// ============== 实时天气 ==============
data class NowWeatherResponse(
    val code: String,
    val updateTime: String?,
    val fxLink: String?,
    val now: NowWeather?,
    val refer: Refer?
) {
    fun isSuccess(): Boolean = code == "200"
}

data class NowWeather(
    val obsTime: String?,          // 观测时间
    val temp: String?,             // 温度
    val feelsLike: String?,        // 体感温度
    val icon: String?,             // 天气图标代码
    val text: String?,             // 天气状况文字
    val wind360: String?,          // 风向360角度
    val windDir: String?,          // 风向
    val windScale: String?,        // 风力等级
    val windSpeed: String?,        // 风速 km/h
    val humidity: String?,         // 湿度百分比
    val precip: String?,           // 当前小时累计降水量 mm
    val pressure: String?,         // 大气压强 hPa
    val vis: String?,              // 能见度 km
    val cloud: String?,            // 云量百分比
    val dew: String?               // 露点温度
)

// ============== 24小时预报 ==============
data class HourlyForecastResponse(
    val code: String,
    val updateTime: String?,
    val fxLink: String?,
    val hourly: List<HourlyWeather>?,
    val refer: Refer?
) {
    fun isSuccess(): Boolean = code == "200"
}

data class HourlyWeather(
    val fxTime: String?,           // 预报时间
    val temp: String?,             // 温度
    val icon: String?,             // 天气图标代码
    val text: String?,             // 天气状况文字
    val wind360: String?,          // 风向360角度
    val windDir: String?,          // 风向
    val windScale: String?,        // 风力等级
    val windSpeed: String?,        // 风速 km/h
    val humidity: String?,         // 湿度百分比
    val pop: String?,              // 降水概率百分比
    val precip: String?,           // 降水量 mm
    val pressure: String?,         // 大气压强 hPa
    val cloud: String?,            // 云量百分比
    val dew: String?               // 露点温度
)

// ============== 7天预报 ==============
data class DailyForecastResponse(
    val code: String,
    val updateTime: String?,
    val fxLink: String?,
    val daily: List<DailyWeather>?,
    val refer: Refer?
) {
    fun isSuccess(): Boolean = code == "200"
}

data class DailyWeather(
    val fxDate: String?,           // 预报日期
    val sunrise: String?,          // 日出时间
    val sunset: String?,           // 日落时间
    val moonrise: String?,         // 月升时间
    val moonset: String?,          // 月落时间
    val moonPhase: String?,        // 月相名称
    val moonPhaseIcon: String?,    // 月相图标代码
    val tempMax: String?,          // 最高温度
    val tempMin: String?,          // 最低温度
    val iconDay: String?,          // 白天天气图标代码
    val textDay: String?,          // 白天天气状况文字
    val iconNight: String?,        // 晚间天气图标代码
    val textNight: String?,        // 晚间天气状况文字
    val wind360Day: String?,       // 白天风向360角度
    val windDirDay: String?,       // 白天风向
    val windScaleDay: String?,     // 白天风力等级
    val windSpeedDay: String?,     // 白天风速 km/h
    val wind360Night: String?,     // 夜间风向360角度
    val windDirNight: String?,     // 夜间风向
    val windScaleNight: String?,   // 夜间风力等级
    val windSpeedNight: String?,   // 夜间风速 km/h
    val humidity: String?,         // 湿度百分比
    val precip: String?,           // 降水量 mm
    val pressure: String?,         // 大气压强 hPa
    val vis: String?,              // 能见度 km
    val cloud: String?,            // 云量百分比
    val uvIndex: String?           // 紫外线强度指数
)

// ============== 空气质量 ==============
data class AirQualityResponse(
    val code: String,
    val updateTime: String?,
    val fxLink: String?,
    val now: AirQualityNow?,
    val refer: Refer?
) {
    fun isSuccess(): Boolean = code == "200"
}

data class AirQualityNow(
    val pubTime: String?,          // 发布时间
    val aqi: String?,              // AQI指数
    val level: String?,            // 空气质量等级
    val category: String?,         // 空气质量类别
    val primary: String?,          // 主要污染物
    val pm10: String?,             // PM10
    val pm2p5: String?,            // PM2.5
    val no2: String?,              // 二氧化氮
    val so2: String?,              // 二氧化硫
    val co: String?,               // 一氧化碳
    val o3: String?                // 臭氧
)

// ============== 天气预警 ==============
data class WeatherWarningResponse(
    val code: String,
    val updateTime: String?,
    val fxLink: String?,
    val warning: List<WeatherWarning>?,
    val refer: Refer?
) {
    fun isSuccess(): Boolean = code == "200"
}

data class WeatherWarning(
    val id: String?,               // 预警ID
    val sender: String?,           // 发布单位
    val pubTime: String?,          // 发布时间
    val title: String?,            // 预警标题
    val status: String?,           // 预警状态
    val level: String?,            // 预警等级
    val type: String?,             // 预警类型
    val typeName: String?,         // 预警类型名称
    val text: String?,             // 预警详情
    val related: String?           // 与本条预警相关联的预警ID
)

// ============== 位置信息 ==============
data class LocationResponse(
    val code: String,
    val location: List<QWeatherLocation>?
) {
    fun isSuccess(): Boolean = code == "200"
}

data class QWeatherLocation(
    val name: String?,             // 地区名称
    val id: String?,               // 地区ID
    val lat: String?,              // 纬度
    val lon: String?,              // 经度
    val adm2: String?,             // 上级行政区划
    val adm1: String?,             // 一级行政区划
    val country: String?,          // 国家
    val tz: String?,               // 时区
    val utcOffset: String?,        // UTC时间偏移
    val isDst: String?,            // 是否处于夏令时
    val type: String?,             // 地区属性
    val rank: String?,             // 地区评分
    val fxLink: String?            // 该地区的天气预报链接
)

// ============== 引用信息 ==============
data class Refer(
    val sources: List<String>?,
    val license: List<String>?
)

// ============== UI 状态模型 ==============
/**
 * 首页天气数据聚合模型
 */
data class HomeWeatherData(
    val location: QWeatherLocation?,
    val currentWeather: NowWeather?,
    val hourlyForecast: List<HourlyWeather>?,
    val dailyForecast: List<DailyWeather>?,
    val airQuality: AirQualityNow?,
    val warnings: List<WeatherWarning>?
)

/**
 * UI 状态封装
 */
sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String, val code: String? = null) : UiState<Nothing>()
}

/**
 * 首页状态
 */
data class HomeUiState(
    val weatherState: UiState<HomeWeatherData> = UiState.Loading,
    val isRefreshing: Boolean = false,
    val lastUpdateTime: String? = null,
    val locationName: String = "定位中..."
)
