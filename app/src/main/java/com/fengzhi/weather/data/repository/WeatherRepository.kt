package com.fengzhi.weather.data.repository

import com.fengzhi.weather.BuildConfig
import com.fengzhi.weather.data.api.QWeatherApi
import com.fengzhi.weather.data.model.*
import com.fengzhi.weather.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 天气数据仓库
 * 负责从和风天气 API 获取数据并聚合
 */
@Singleton
class WeatherRepository @Inject constructor(
    private val qWeatherApi: QWeatherApi
) {
    // API Key - 从 BuildConfig 或 Constants 获取
    private val apiKey: String
        get() = BuildConfig.QWEATHER_API_KEY.ifEmpty { Constants.QWEATHER_API_KEY }

    /**
     * 获取首页所有天气数据
     * 并行请求多个接口以提高性能
     * @param latitude 纬度
     * @param longitude 经度
     */
    suspend fun getHomeWeatherData(
        latitude: Double,
        longitude: Double
    ): Result<HomeWeatherData> = withContext(Dispatchers.IO) {
        try {
            val location = "$longitude,$latitude"
            
            coroutineScope {
                // 并行请求所有数据
                val locationDeferred = async { fetchLocation(location) }
                val currentWeatherDeferred = async { fetchCurrentWeather(location) }
                val hourlyForecastDeferred = async { fetchHourlyForecast(location) }
                val dailyForecastDeferred = async { fetchDailyForecast(location) }
                val airQualityDeferred = async { fetchAirQuality(location) }
                val warningsDeferred = async { fetchWeatherWarnings(location) }
                
                // 等待所有请求完成
                val locationResult = locationDeferred.await()
                val currentWeatherResult = currentWeatherDeferred.await()
                val hourlyForecastResult = hourlyForecastDeferred.await()
                val dailyForecastResult = dailyForecastDeferred.await()
                val airQualityResult = airQualityDeferred.await()
                val warningsResult = warningsDeferred.await()
                
                Result.success(
                    HomeWeatherData(
                        location = locationResult.getOrNull(),
                        currentWeather = currentWeatherResult.getOrNull(),
                        hourlyForecast = hourlyForecastResult.getOrNull(),
                        dailyForecast = dailyForecastResult.getOrNull(),
                        airQuality = airQualityResult.getOrNull(),
                        warnings = warningsResult.getOrNull()
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取当前位置信息
     */
    suspend fun getLocation(latitude: Double, longitude: Double): Result<QWeatherLocation> {
        return withContext(Dispatchers.IO) {
            fetchLocation("$longitude,$latitude")
        }
    }

    /**
     * 搜索城市
     */
    suspend fun searchCity(query: String): Result<List<QWeatherLocation>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = qWeatherApi.searchCity(query, apiKey)
                if (response.isSuccessful && response.body()?.isSuccess() == true) {
                    Result.success(response.body()?.location ?: emptyList())
                } else {
                    Result.failure(Exception("搜索失败: ${response.body()?.code}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 获取实时天气
     */
    suspend fun getCurrentWeather(location: String): Result<NowWeather> {
        return withContext(Dispatchers.IO) {
            fetchCurrentWeather(location)
        }
    }

    /**
     * 获取24小时预报
     */
    suspend fun getHourlyForecast(location: String): Result<List<HourlyWeather>> {
        return withContext(Dispatchers.IO) {
            fetchHourlyForecast(location)
        }
    }

    /**
     * 获取7天预报
     */
    suspend fun getDailyForecast(location: String): Result<List<DailyWeather>> {
        return withContext(Dispatchers.IO) {
            fetchDailyForecast(location)
        }
    }

    /**
     * 获取空气质量
     */
    suspend fun getAirQuality(location: String): Result<AirQualityNow> {
        return withContext(Dispatchers.IO) {
            fetchAirQuality(location)
        }
    }

    /**
     * 获取天气预警
     */
    suspend fun getWeatherWarnings(location: String): Result<List<WeatherWarning>> {
        return withContext(Dispatchers.IO) {
            fetchWeatherWarnings(location)
        }
    }

    // ============== 私有方法 ==============

    private suspend fun fetchLocation(location: String): Result<QWeatherLocation?> {
        return try {
            val response = qWeatherApi.getLocationByCoords(location, apiKey)
            handleResponse(response) { it.location?.firstOrNull() }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun fetchCurrentWeather(location: String): Result<NowWeather?> {
        return try {
            val response = qWeatherApi.getCurrentWeather(location, apiKey)
            handleResponse(response) { it.now }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun fetchHourlyForecast(location: String): Result<List<HourlyWeather>?> {
        return try {
            val response = qWeatherApi.getHourlyForecast(location, apiKey)
            handleResponse(response) { it.hourly }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun fetchDailyForecast(location: String): Result<List<DailyWeather>?> {
        return try {
            val response = qWeatherApi.getDailyForecast(location, apiKey)
            handleResponse(response) { it.daily }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun fetchAirQuality(location: String): Result<AirQualityNow?> {
        return try {
            val response = qWeatherApi.getAirQuality(location, apiKey)
            handleResponse(response) { it.now }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun fetchWeatherWarnings(location: String): Result<List<WeatherWarning>?> {
        return try {
            val response = qWeatherApi.getWeatherWarnings(location, apiKey)
            handleResponse(response) { it.warning }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 统一处理 API 响应
     */
    private inline fun <T, R> handleResponse(
        response: Response<T>,
        transform: (T) -> R
    ): Result<R> {
        return if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                // 检查业务状态码
                val code = when (body) {
                    is NowWeatherResponse -> body.code
                    is HourlyForecastResponse -> body.code
                    is DailyForecastResponse -> body.code
                    is AirQualityResponse -> body.code
                    is WeatherWarningResponse -> body.code
                    is LocationResponse -> body.code
                    else -> "200"
                }
                
                if (code == "200") {
                    Result.success(transform(body))
                } else {
                    Result.failure(QWeatherException(code, getErrorMessage(code)))
                }
            } else {
                Result.failure(Exception("响应体为空"))
            }
        } else {
            Result.failure(Exception("网络请求失败: ${response.code()}"))
        }
    }

    /**
     * 获取错误信息
     */
    private fun getErrorMessage(code: String): String {
        return when (code) {
            "400" -> "请求错误"
            "401" -> "认证错误，API Key 无效"
            "402" -> "超过访问次数或余额不足"
            "403" -> "无访问权限"
            "404" -> "查询的数据不存在"
            "429" -> "请求过于频繁"
            "500" -> "服务器内部错误"
            else -> "未知错误: $code"
        }
    }
}

/**
 * 和风天气异常
 */
class QWeatherException(
    val code: String,
    message: String
) : Exception(message)
