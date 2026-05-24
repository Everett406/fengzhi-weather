package com.fengzhi.weather.ui.screens.home.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fengzhi.weather.R
import com.fengzhi.weather.data.model.NowWeather
import com.fengzhi.weather.ui.theme.OrangePrimary
import kotlinx.coroutines.delay

/**
 * 当前天气卡片
 * 显示当前温度、天气状况、体感温度等信息
 */
@Composable
fun CurrentWeatherCard(
    weather: NowWeather?,
    locationName: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 位置名称
            Text(
                text = locationName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 天气图标
            weather?.icon?.let { iconCode ->
                WeatherIcon(
                    iconCode = iconCode,
                    isDay = true,
                    modifier = Modifier.size(100.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 温度 - 带动画
            AnimatedTemperature(
                temperature = weather?.temp,
                fontSize = 72.sp
            )

            // 天气状况
            Text(
                text = weather?.text ?: "--",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 详细信息行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WeatherDetailItem(
                    label = "体感",
                    value = weather?.feelsLike?.let { "${it}°" } ?: "--"
                )
                WeatherDetailItem(
                    label = "湿度",
                    value = weather?.humidity?.let { "${it}%" } ?: "--"
                )
                WeatherDetailItem(
                    label = "风速",
                    value = weather?.windSpeed?.let { "${it}km/h" } ?: "--"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 风向和气压
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = weather?.windDir ?: "--",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = " · ",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = weather?.windScale?.let { "${it}级" } ?: "--",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = " · ",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = weather?.pressure?.let { "${it}hPa" } ?: "--",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 带动画的温度显示
 */
@Composable
fun AnimatedTemperature(
    temperature: String?,
    fontSize: androidx.compose.ui.unit.TextUnit = 64.sp,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    val tempValue = temperature?.toIntOrNull() ?: 0
    
    var displayValue by remember { mutableStateOf(0) }
    val animatedValue by animateIntAsState(
        targetValue = displayValue,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "temperature_animation"
    )

    LaunchedEffect(tempValue) {
        displayValue = tempValue
    }

    Row(
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = animatedValue.toString(),
            fontSize = fontSize,
            fontWeight = FontWeight.Light,
            color = color
        )
        Text(
            text = "°",
            fontSize = fontSize * 0.5f,
            fontWeight = FontWeight.Light,
            color = color
        )
    }
}

/**
 * 天气详情项
 */
@Composable
fun WeatherDetailItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 天气图标组件
 * 根据和风天气图标代码显示对应图标
 */
@Composable
fun WeatherIcon(
    iconCode: String,
    isDay: Boolean = true,
    modifier: Modifier = Modifier
) {
    // 根据图标代码获取对应的 drawable 资源 ID
    // 这里使用简化的映射，实际项目中应该有完整的图标资源
    val iconResId = getWeatherIconResource(iconCode, isDay)

    AnimatedContent(
        targetState = iconCode,
        transitionSpec = {
            fadeIn(animationSpec = tween(300)) togetherWith
                    fadeOut(animationSpec = tween(300))
        },
        label = "weather_icon_animation"
    ) { targetIcon ->
        Image(
            painter = painterResource(id = iconResId),
            contentDescription = "Weather icon",
            modifier = modifier
        )
    }
}

/**
 * 获取天气图标资源
 * 简化版本，实际项目需要完整的图标资源
 */
fun getWeatherIconResource(iconCode: String, isDay: Boolean): Int {
    return when {
        // 晴天
        iconCode == "100" -> if (isDay) R.drawable.ic_sunny else R.drawable.ic_clear_night
        iconCode == "150" -> R.drawable.ic_clear_night
        
        // 多云
        iconCode == "101" -> if (isDay) R.drawable.ic_partly_cloudy else R.drawable.ic_partly_cloudy_night
        iconCode == "102" -> R.drawable.ic_cloudy
        iconCode == "103" -> R.drawable.ic_partly_cloudy
        iconCode == "104" -> R.drawable.ic_overcast
        iconCode == "151" -> R.drawable.ic_partly_cloudy_night
        iconCode == "153" -> R.drawable.ic_cloudy
        iconCode == "154" -> R.drawable.ic_overcast
        
        // 雨
        iconCode in listOf("300", "301", "302", "303", "304", "305", "306", "307", "308", "309", "310", "311", "312", "313", "314", "315", "316", "317", "318") -> 
            R.drawable.ic_rain
        iconCode in listOf("350", "351", "352") -> R.drawable.ic_rain_night
        iconCode == "303" -> R.drawable.ic_thunderstorm
        iconCode == "304" -> R.drawable.ic_thunderstorm
        
        // 雪
        iconCode in listOf("400", "401", "402", "403", "404", "405", "406", "407", "408", "409", "410") -> 
            R.drawable.ic_snow
        iconCode in listOf("450", "451", "452", "453", "454", "455", "456", "457") -> 
            R.drawable.ic_snow_night
        
        // 雾霾沙尘
        iconCode == "500" -> R.drawable.ic_fog
        iconCode == "501" -> R.drawable.ic_fog
        iconCode == "502" -> R.drawable.ic_haze
        iconCode == "503" -> R.drawable.ic_sand
        iconCode == "504" -> R.drawable.ic_sand
        iconCode == "507" -> R.drawable.ic_wind
        iconCode == "508" -> R.drawable.ic_wind
        
        // 默认
        else -> if (isDay) R.drawable.ic_sunny else R.drawable.ic_clear_night
    }
}
