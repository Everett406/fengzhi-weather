package com.fengzhi.weather.ui.screens.home

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fengzhi.weather.data.model.HomeUiState
import com.fengzhi.weather.data.model.HomeWeatherData
import com.fengzhi.weather.data.model.UiState
import com.fengzhi.weather.data.repository.WeatherRepository
import com.fengzhi.weather.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // 位置相关偏好键
    private val keyLatitude = doublePreferencesKey(Constants.PREF_LAST_LATITUDE)
    private val keyLongitude = doublePreferencesKey(Constants.PREF_LAST_LONGITUDE)
    private val keyCityName = stringPreferencesKey(Constants.PREF_CITY_NAME)

    init {
        loadWeatherData()
    }

    /**
     * 加载天气数据
     */
    fun loadWeatherData() {
        viewModelScope.launch {
            // 从 DataStore 获取保存的位置
            val preferences = dataStore.data.first()
            val latitude = preferences[keyLatitude] ?: Constants.DEFAULT_LATITUDE
            val longitude = preferences[keyLongitude] ?: Constants.DEFAULT_LONGITUDE
            val cityName = preferences[keyCityName] ?: Constants.DEFAULT_CITY_NAME

            _uiState.update { it.copy(locationName = cityName) }
            
            fetchWeatherData(latitude, longitude)
        }
    }

    /**
     * 刷新天气数据
     */
    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            
            val preferences = dataStore.data.first()
            val latitude = preferences[keyLatitude] ?: Constants.DEFAULT_LATITUDE
            val longitude = preferences[keyLongitude] ?: Constants.DEFAULT_LONGITUDE
            
            fetchWeatherData(latitude, longitude, isRefresh = true)
        }
    }

    /**
     * 更新位置
     */
    fun updateLocation(latitude: Double, longitude: Double, cityName: String) {
        viewModelScope.launch {
            // 保存到 DataStore
            dataStore.edit { preferences ->
                preferences[keyLatitude] = latitude
                preferences[keyLongitude] = longitude
                preferences[keyCityName] = cityName
            }

            _uiState.update { it.copy(locationName = cityName) }
            fetchWeatherData(latitude, longitude)
        }
    }

    /**
     * 重试加载
     */
    fun retry() {
        loadWeatherData()
    }

    /**
     * 获取天气数据
     */
    private suspend fun fetchWeatherData(
        latitude: Double,
        longitude: Double,
        isRefresh: Boolean = false
    ) {
        if (!isRefresh) {
            _uiState.update { it.copy(weatherState = UiState.Loading) }
        }

        val result = weatherRepository.getHomeWeatherData(latitude, longitude)

        result.fold(
            onSuccess = { data ->
                val locationName = data.location?.name ?: _uiState.value.locationName
                val updateTime = SimpleDateFormat(
                    "HH:mm",
                    Locale.getDefault()
                ).format(Date())

                _uiState.update {
                    it.copy(
                        weatherState = UiState.Success(data),
                        isRefreshing = false,
                        lastUpdateTime = updateTime,
                        locationName = locationName
                    )
                }
            },
            onFailure = { error ->
                _uiState.update {
                    it.copy(
                        weatherState = UiState.Error(
                            message = error.message ?: "加载失败，请重试",
                            code = (error as? com.fengzhi.weather.data.repository.QWeatherException)?.code
                        ),
                        isRefreshing = false
                    )
                }
            }
        )
    }

    /**
     * 获取温度显示值
     */
    fun getTemperatureDisplay(temp: String?): String {
        return if (temp.isNullOrEmpty()) "--" else "${temp}°"
    }

    /**
     * 获取风速显示值
     */
    fun getWindSpeedDisplay(speed: String?): String {
        return if (speed.isNullOrEmpty()) "--" else "${speed}km/h"
    }

    /**
     * 获取湿度显示值
     */
    fun getHumidityDisplay(humidity: String?): String {
        return if (humidity.isNullOrEmpty()) "--" else "${humidity}%"
    }

    /**
     * 获取能见度显示值
     */
    fun getVisibilityDisplay(vis: String?): String {
        return if (vis.isNullOrEmpty()) "--" else "${vis}km"
    }

    /**
     * 获取气压显示值
     */
    fun getPressureDisplay(pressure: String?): String {
        return if (pressure.isNullOrEmpty()) "--" else "${pressure}hPa"
    }

    /**
     * 获取 AQI 等级颜色
     */
    fun getAqiColor(aqi: Int): androidx.compose.ui.graphics.Color {
        return when {
            aqi <= 50 -> androidx.compose.ui.graphics.Color(0xFF4CAF50)  // 优 - 绿色
            aqi <= 100 -> androidx.compose.ui.graphics.Color(0xFFCDDC39) // 良 - 黄绿色
            aqi <= 150 -> androidx.compose.ui.graphics.Color(0xFFFF9800) // 轻度 - 橙色
            aqi <= 200 -> androidx.compose.ui.graphics.Color(0xFFF44336) // 中度 - 红色
            aqi <= 300 -> androidx.compose.ui.graphics.Color(0xFF9C27B0) // 重度 - 紫色
            else -> androidx.compose.ui.graphics.Color(0xFF795548)       // 严重 - 褐红色
        }
    }

    /**
     * 获取预警等级颜色
     */
    fun getWarningColor(level: String?): androidx.compose.ui.graphics.Color {
        return when (level) {
            Constants.WarningLevel.RED -> androidx.compose.ui.graphics.Color(0xFFE53935)
            Constants.WarningLevel.ORANGE -> androidx.compose.ui.graphics.Color(0xFFFF9800)
            Constants.WarningLevel.YELLOW -> androidx.compose.ui.graphics.Color(0xFFFDD835)
            Constants.WarningLevel.BLUE -> androidx.compose.ui.graphics.Color(0xFF1E88E5)
            else -> androidx.compose.ui.graphics.Color(0xFF9E9E9E)
        }
    }
}
