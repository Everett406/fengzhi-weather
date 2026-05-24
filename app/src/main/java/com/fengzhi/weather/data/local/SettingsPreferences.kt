package com.fengzhi.weather.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 应用设置偏好管理
 * 使用 DataStore 存储主题模式、刷新间隔等设置
 */
@Singleton
class SettingsPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        private val KEY_REFRESH_INTERVAL = intPreferencesKey("refresh_interval")
        private val KEY_TEMPERATURE_UNIT = stringPreferencesKey("temperature_unit")
        private val KEY_WIND_UNIT = stringPreferencesKey("wind_unit")
        
        // 主题模式
        const val THEME_SYSTEM = "system"
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"
        
        // 刷新间隔（分钟）
        const val REFRESH_10_MINUTES = 10
        const val REFRESH_30_MINUTES = 30
        const val REFRESH_1_HOUR = 60
        const val REFRESH_MANUAL = -1 // 手动刷新
        
        // 温度单位
        const val UNIT_CELSIUS = "celsius"
        const val UNIT_FAHRENHEIT = "fahrenheit"
        
        // 风速单位
        const val UNIT_KMH = "kmh"
        const val UNIT_MS = "ms"
        const val UNIT_MPH = "mph"
    }

    /**
     * 获取主题模式
     */
    val themeMode: Flow<String> = dataStore.data.map { preferences ->
        preferences[KEY_THEME_MODE] ?: THEME_SYSTEM
    }

    /**
     * 获取刷新间隔（分钟）
     */
    val refreshInterval: Flow<Int> = dataStore.data.map { preferences ->
        preferences[KEY_REFRESH_INTERVAL] ?: REFRESH_10_MINUTES
    }

    /**
     * 获取温度单位
     */
    val temperatureUnit: Flow<String> = dataStore.data.map { preferences ->
        preferences[KEY_TEMPERATURE_UNIT] ?: UNIT_CELSIUS
    }

    /**
     * 获取风速单位
     */
    val windUnit: Flow<String> = dataStore.data.map { preferences ->
        preferences[KEY_WIND_UNIT] ?: UNIT_KMH
    }

    /**
     * 设置主题模式
     */
    suspend fun setThemeMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = mode
        }
    }

    /**
     * 设置刷新间隔
     */
    suspend fun setRefreshInterval(intervalMinutes: Int) {
        dataStore.edit { preferences ->
            preferences[KEY_REFRESH_INTERVAL] = intervalMinutes
        }
    }

    /**
     * 设置温度单位
     */
    suspend fun setTemperatureUnit(unit: String) {
        dataStore.edit { preferences ->
            preferences[KEY_TEMPERATURE_UNIT] = unit
        }
    }

    /**
     * 设置风速单位
     */
    suspend fun setWindUnit(unit: String) {
        dataStore.edit { preferences ->
            preferences[KEY_WIND_UNIT] = unit
        }
    }

    /**
     * 主题模式显示名称
     */
    fun getThemeModeDisplayName(mode: String): String {
        return when (mode) {
            THEME_SYSTEM -> "跟随系统"
            THEME_LIGHT -> "浅色模式"
            THEME_DARK -> "深色模式"
            else -> "跟随系统"
        }
    }

    /**
     * 刷新间隔显示名称
     */
    fun getRefreshIntervalDisplayName(intervalMinutes: Int): String {
        return when (intervalMinutes) {
            REFRESH_10_MINUTES -> "10 分钟"
            REFRESH_30_MINUTES -> "30 分钟"
            REFRESH_1_HOUR -> "1 小时"
            REFRESH_MANUAL -> "手动刷新"
            else -> "10 分钟"
        }
    }

    /**
     * 温度单位显示名称
     */
    fun getTemperatureUnitDisplayName(unit: String): String {
        return when (unit) {
            UNIT_CELSIUS -> "摄氏度 (°C)"
            UNIT_FAHRENHEIT -> "华氏度 (°F)"
            else -> "摄氏度 (°C)"
        }
    }

    /**
     * 风速单位显示名称
     */
    fun getWindUnitDisplayName(unit: String): String {
        return when (unit) {
            UNIT_KMH -> "公里/小时 (km/h)"
            UNIT_MS -> "米/秒 (m/s)"
            UNIT_MPH -> "英里/小时 (mph)"
            else -> "公里/小时 (km/h)"
        }
    }
}
