package com.fengzhi.weather.data.api

import com.fengzhi.weather.data.model.WeatherData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {

    @GET("weather/current")
    suspend fun getCurrentWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("units") units: String = "metric"
    ): Response<WeatherData>

    @GET("weather/forecast")
    suspend fun getForecast(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("days") days: Int = 7,
        @Query("units") units: String = "metric"
    ): Response<WeatherData>

    @GET("weather/search")
    suspend fun searchLocation(
        @Query("q") query: String
    ): Response<List<com.fengzhi.weather.data.model.Location>>
}
