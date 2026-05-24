package com.fengzhi.weather.data.api

import com.fengzhi.weather.data.model.*
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 和风天气 API 接口
 * API 文档: https://dev.qweather.com/docs/api/
 */
interface QWeatherApi {

    /**
     * 实时天气
     * GET /v7/weather/now
     * @param location 需要查询地区的LocationID或经纬度坐标（经度,纬度）
     * @param key 用户认证key
     * @param lang 多语言设置，默认中文
     */
    @GET("weather/now")
    suspend fun getCurrentWeather(
        @Query("location") location: String,
        @Query("key") key: String,
        @Query("lang") lang: String = "zh"
    ): Response<NowWeatherResponse>

    /**
     * 24小时天气预报
     * GET /v7/weather/24h
     * @param location 需要查询地区的LocationID或经纬度坐标
     * @param key 用户认证key
     * @param lang 多语言设置
     */
    @GET("weather/24h")
    suspend fun getHourlyForecast(
        @Query("location") location: String,
        @Query("key") key: String,
        @Query("lang") lang: String = "zh"
    ): Response<HourlyForecastResponse>

    /**
     * 7天天气预报
     * GET /v7/weather/7d
     * @param location 需要查询地区的LocationID或经纬度坐标
     * @param key 用户认证key
     * @param lang 多语言设置
     */
    @GET("weather/7d")
    suspend fun getDailyForecast(
        @Query("location") location: String,
        @Query("key") key: String,
        @Query("lang") lang: String = "zh"
    ): Response<DailyForecastResponse>

    /**
     * 实时空气质量
     * GET /v7/air/now
     * @param location 需要查询地区的LocationID或经纬度坐标
     * @param key 用户认证key
     * @param lang 多语言设置
     */
    @GET("air/now")
    suspend fun getAirQuality(
        @Query("location") location: String,
        @Query("key") key: String,
        @Query("lang") lang: String = "zh"
    ): Response<AirQualityResponse>

    /**
     * 天气灾害预警
     * GET /v7/warning/now
     * @param location 需要查询地区的LocationID或经纬度坐标
     * @param key 用户认证key
     * @param lang 多语言设置
     */
    @GET("warning/now")
    suspend fun getWeatherWarnings(
        @Query("location") location: String,
        @Query("key") key: String,
        @Query("lang") lang: String = "zh"
    ): Response<WeatherWarningResponse>

    /**
     * 城市搜索 - 经纬度反查
     * GET /v7/geo/poi/location
     * @param location 经纬度坐标
     * @param key 用户认证key
     * @param type POI类型，城市为 city
     */
    @GET("geo/poi/location")
    suspend fun getLocationByCoords(
        @Query("location") location: String,
        @Query("key") key: String,
        @Query("type") type: String = "city"
    ): Response<LocationResponse>

    /**
     * 城市搜索 - 关键字搜索
     * GET /v7/geo/searchCity
     * @param location 城市名称
     * @param key 用户认证key
     * @param adm 城市上级行政区
     * @param number 返回结果数量
     */
    @GET("geo/searchCity")
    suspend fun searchCity(
        @Query("location") location: String,
        @Query("key") key: String,
        @Query("adm") adm: String? = null,
        @Query("number") number: Int = 10
    ): Response<LocationResponse>

    /**
     * 热门城市查询
     * GET /v7/geo/topCity
     * @param key 用户认证key
     * @param range 查询范围
     * @param number 返回结果数量
     */
    @GET("geo/topCity")
    suspend fun getTopCities(
        @Query("key") key: String,
        @Query("range") range: String = "cn",
        @Query("number") number: Int = 20
    ): Response<LocationResponse>
}
