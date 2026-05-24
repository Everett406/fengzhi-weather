package com.fengzhi.weather.data.model

import com.google.gson.annotations.SerializedName

data class CurrentWeather(
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Int,
    val pressure: Int,
    val windSpeed: Double,
    val windDirection: String,
    val visibility: Double,
    val uvIndex: Int,
    val condition: String,
    val description: String,
    val icon: String,
    val timestamp: Long
)

data class HourlyForecast(
    val time: Long,
    val temperature: Double,
    val condition: String,
    val icon: String,
    val precipitationProbability: Int
)

data class DailyForecast(
    val date: Long,
    val maxTemp: Double,
    val minTemp: Double,
    val condition: String,
    val icon: String,
    val precipitationProbability: Int,
    val sunrise: Long,
    val sunset: Long
)

data class AirQuality(
    val aqi: Int,
    val pm25: Double,
    val pm10: Double,
    val o3: Double,
    val no2: Double,
    val so2: Double,
    val co: Double,
    val level: String,
    val advice: String
)

data class Location(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String,
    val adminArea: String? = null
)

data class WeatherData(
    val location: Location,
    val current: CurrentWeather,
    val hourly: List<HourlyForecast>,
    val daily: List<DailyForecast>,
    val airQuality: AirQuality?
)
