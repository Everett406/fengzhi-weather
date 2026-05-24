package com.fengzhi.weather.utils

object Constants {
    const val APP_NAME = "风止天气"
    const val VERSION_NAME = "1.0.0"
    const val VERSION_CODE = 1

    // DataStore Keys
    const val PREF_TEMP_UNIT = "temperature_unit"
    const val PREF_WIND_UNIT = "wind_unit"
    const val PREF_THEME_MODE = "theme_mode"
    const val PREF_LAST_LOCATION = "last_location"
    const val PREF_REFRESH_INTERVAL = "refresh_interval"
    const val PREF_CITY_LIST = "city_list"
    const val PREF_CURRENT_CITY_ID = "current_city_id"

    // Temperature Units
    const val UNIT_CELSIUS = "celsius"
    const val UNIT_FAHRENHEIT = "fahrenheit"

    // Wind Units
    const val UNIT_KMH = "kmh"
    const val UNIT_MPH = "mph"
    const val UNIT_MS = "ms"

    // Theme Modes
    const val THEME_SYSTEM = "system"
    const val THEME_LIGHT = "light"
    const val THEME_DARK = "dark"

    // Refresh Intervals (minutes)
    const val REFRESH_10_MINUTES = 10
    const val REFRESH_30_MINUTES = 30
    const val REFRESH_1_HOUR = 60
    const val REFRESH_MANUAL = -1

    // Location
    const val DEFAULT_LATITUDE = 39.9042
    const val DEFAULT_LONGITUDE = 116.4074

    // QWeather API Key (fallback)
    const val QWEATHER_API_KEY = ""

    // Data Sources
    object DataSources {
        const val QWEATHER_NAME = "和风天气"
        const val QWEATHER_URL = "https://www.qweather.com/"
        
        const val NICT_NAME = "NICT/JMA"
        const val NICT_URL = "https://himawari8.nict.go.jp/"
        
        const val RAINVIEWER_NAME = "RainViewer"
        const val RAINVIEWER_URL = "https://www.rainviewer.com/"
    }

    // Satellite Cloud Image
    object Satellite {
        // NICT Himawari API
        const val NICT_BASE_URL = "https://himawari8-dl.nict.go.jp/"
        
        // Image update interval (minutes)
        const val UPDATE_INTERVAL_MINUTES = 10
        
        // Timeline history hours
        const val TIMELINE_HOURS = 24
        
        // Default scale (2x2 tiles)
        const val DEFAULT_SCALE = 2
        
        // Tile size in pixels
        const val TILE_SIZE = 550
        
        // Cache duration in hours
        const val CACHE_DURATION_HOURS = 24
        
        // Animation frame delay (milliseconds)
        const val DEFAULT_ANIMATION_DELAY = 500L
        
        // Max zoom level
        const val MAX_ZOOM = 5f
        
        // Min zoom level
        const val MIN_ZOOM = 0.5f
    }

    // GitHub
    object GitHub {
        const val REPO_URL = "https://github.com/your-username/fengzhi-weather"
        const val LICENSE_URL = "https://github.com/your-username/fengzhi-weather/blob/main/LICENSE"
    }
}
