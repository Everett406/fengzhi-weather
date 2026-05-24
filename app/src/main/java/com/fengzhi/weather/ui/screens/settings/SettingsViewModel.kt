package com.fengzhi.weather.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fengzhi.weather.data.local.CityPreferences
import com.fengzhi.weather.data.local.SettingsPreferences
import com.fengzhi.weather.data.model.City
import com.fengzhi.weather.data.model.QWeatherCityLocation
import com.fengzhi.weather.data.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 设置页面 ViewModel
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val cityPreferences: CityPreferences,
    private val settingsPreferences: SettingsPreferences,
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    // 城市列表
    val cityList: StateFlow<List<City>> = cityPreferences.cityList
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 当前城市 ID
    val currentCityId: StateFlow<String?> = cityPreferences.currentCityId
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // 主题模式
    val themeMode: StateFlow<String> = settingsPreferences.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsPreferences.THEME_SYSTEM
        )

    // 刷新间隔
    val refreshInterval: StateFlow<Int> = settingsPreferences.refreshInterval
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsPreferences.REFRESH_10_MINUTES
        )

    // 温度单位
    val temperatureUnit: StateFlow<String> = settingsPreferences.temperatureUnit
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsPreferences.UNIT_CELSIUS
        )

    // 风速单位
    val windUnit: StateFlow<String> = settingsPreferences.windUnit
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsPreferences.UNIT_KMH
        )

    // 搜索状态
    private val _searchState = MutableStateFlow<SearchState>(SearchState.Idle)
    val searchState: StateFlow<SearchState> = _searchState.asStateFlow()

    // 搜索结果
    private val _searchResults = MutableStateFlow<List<QWeatherCityLocation>>(emptyList())
    val searchResults: StateFlow<List<QWeatherCityLocation>> = _searchResults.asStateFlow()

    // 搜索城市
    fun searchCity(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            _searchState.value = SearchState.Idle
            return
        }

        viewModelScope.launch {
            _searchState.value = SearchState.Loading
            val result = weatherRepository.searchCity(query)
            
            result.fold(
                onSuccess = { cities ->
                    _searchResults.value = cities
                    _searchState.value = if (cities.isEmpty()) {
                        SearchState.Empty
                    } else {
                        SearchState.Success
                    }
                },
                onFailure = { error ->
                    _searchState.value = SearchState.Error(error.message ?: "搜索失败")
                }
            )
        }
    }

    // 添加城市
    fun addCity(location: QWeatherCityLocation) {
        viewModelScope.launch {
            val city = location.toCity()
            cityPreferences.addCity(city)
            // 清空搜索结果
            _searchResults.value = emptyList()
            _searchState.value = SearchState.Idle
        }
    }

    // 删除城市
    fun deleteCity(cityId: String) {
        viewModelScope.launch {
            cityPreferences.deleteCity(cityId)
        }
    }

    // 设置当前城市
    fun setCurrentCity(cityId: String) {
        viewModelScope.launch {
            cityPreferences.setCurrentCity(cityId)
        }
    }

    // 设置主题模式
    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            settingsPreferences.setThemeMode(mode)
        }
    }

    // 设置刷新间隔
    fun setRefreshInterval(intervalMinutes: Int) {
        viewModelScope.launch {
            settingsPreferences.setRefreshInterval(intervalMinutes)
        }
    }

    // 设置温度单位
    fun setTemperatureUnit(unit: String) {
        viewModelScope.launch {
            settingsPreferences.setTemperatureUnit(unit)
        }
    }

    // 设置风速单位
    fun setWindUnit(unit: String) {
        viewModelScope.launch {
            settingsPreferences.setWindUnit(unit)
        }
    }

    // 清空搜索
    fun clearSearch() {
        _searchResults.value = emptyList()
        _searchState.value = SearchState.Idle
    }

    // 获取主题模式显示名称
    fun getThemeModeDisplayName(mode: String): String {
        return settingsPreferences.getThemeModeDisplayName(mode)
    }

    // 获取刷新间隔显示名称
    fun getRefreshIntervalDisplayName(intervalMinutes: Int): String {
        return settingsPreferences.getRefreshIntervalDisplayName(intervalMinutes)
    }

    // 获取温度单位显示名称
    fun getTemperatureUnitDisplayName(unit: String): String {
        return settingsPreferences.getTemperatureUnitDisplayName(unit)
    }

    // 获取风速单位显示名称
    fun getWindUnitDisplayName(unit: String): String {
        return settingsPreferences.getWindUnitDisplayName(unit)
    }
}

/**
 * 搜索状态
 */
sealed class SearchState {
    data object Idle : SearchState()
    data object Loading : SearchState()
    data object Success : SearchState()
    data object Empty : SearchState()
    data class Error(val message: String) : SearchState()
}
