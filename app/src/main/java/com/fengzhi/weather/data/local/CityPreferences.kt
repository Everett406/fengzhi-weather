package com.fengzhi.weather.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.fengzhi.weather.data.model.City
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 城市偏好设置管理
 * 使用 DataStore 存储城市列表和当前选中城市
 */
@Singleton
class CityPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val gson = Gson()

    companion object {
        private val KEY_CITY_LIST = stringPreferencesKey("city_list")
        private val KEY_CURRENT_CITY_ID = stringPreferencesKey("current_city_id")
    }

    /**
     * 获取城市列表
     */
    val cityList: Flow<List<City>> = dataStore.data.map { preferences ->
        val json = preferences[KEY_CITY_LIST]
        if (json.isNullOrEmpty()) {
            emptyList()
        } else {
            try {
                val type = object : TypeToken<List<City>>() {}.type
                gson.fromJson<List<City>>(json, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    /**
     * 获取当前选中城市 ID
     */
    val currentCityId: Flow<String?> = dataStore.data.map { preferences ->
        preferences[KEY_CURRENT_CITY_ID]
    }

    /**
     * 获取当前选中城市
     */
    val currentCity: Flow<City?> = dataStore.data.map { preferences ->
        val json = preferences[KEY_CITY_LIST]
        val currentId = preferences[KEY_CURRENT_CITY_ID]
        
        if (json.isNullOrEmpty() || currentId.isNullOrEmpty()) {
            null
        } else {
            try {
                val type = object : TypeToken<List<City>>() {}.type
                val cities = gson.fromJson<List<City>>(json, type) ?: emptyList()
                cities.find { it.id == currentId }
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * 保存城市列表
     */
    suspend fun saveCityList(cities: List<City>) {
        dataStore.edit { preferences ->
            val json = gson.toJson(cities)
            preferences[KEY_CITY_LIST] = json
        }
    }

    /**
     * 添加城市
     * @return 是否添加成功（如果城市已存在则返回 false）
     */
    suspend fun addCity(city: City): Boolean {
        val currentList = getCityListOnce()
        
        // 检查城市是否已存在
        if (currentList.any { it.id == city.id }) {
            return false
        }
        
        val newList = currentList + city
        saveCityList(newList)
        
        // 如果是第一个城市，自动设为当前城市
        if (currentList.isEmpty()) {
            setCurrentCity(city.id)
        }
        
        return true
    }

    /**
     * 删除城市
     */
    suspend fun deleteCity(cityId: String) {
        val currentList = getCityListOnce()
        val currentId = getCurrentCityIdOnce()
        
        val newList = currentList.filter { it.id != cityId }
        saveCityList(newList)
        
        // 如果删除的是当前城市，自动选择第一个城市
        if (currentId == cityId && newList.isNotEmpty()) {
            setCurrentCity(newList.first().id)
        }
    }

    /**
     * 设置当前城市
     */
    suspend fun setCurrentCity(cityId: String) {
        dataStore.edit { preferences ->
            preferences[KEY_CURRENT_CITY_ID] = cityId
        }
    }

    /**
     * 清空所有城市
     */
    suspend fun clearCities() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_CITY_LIST)
            preferences.remove(KEY_CURRENT_CITY_ID)
        }
    }

    /**
     * 一次性获取城市列表
     */
    private suspend fun getCityListOnce(): List<City> {
        var result: List<City> = emptyList()
        dataStore.data.map { preferences ->
            val json = preferences[KEY_CITY_LIST]
            if (!json.isNullOrEmpty()) {
                try {
                    val type = object : TypeToken<List<City>>() {}.type
                    result = gson.fromJson<List<City>>(json, type) ?: emptyList()
                } catch (e: Exception) {
                    result = emptyList()
                }
            }
        }.collect { }
        return result
    }

    /**
     * 一次性获取当前城市 ID
     */
    private suspend fun getCurrentCityIdOnce(): String? {
        var result: String? = null
        dataStore.data.map { preferences ->
            result = preferences[KEY_CURRENT_CITY_ID]
        }.collect { }
        return result
    }
}
