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

    // Location
    const val DEFAULT_LATITUDE = 39.9042
    const val DEFAULT_LONGITUDE = 116.4074
}
