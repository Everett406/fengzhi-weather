package com.fengzhi.weather.ui.screens.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fengzhi.weather.data.model.HourlyWeather
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 24小时预报卡片
 * 水平滚动显示未来24小时天气预报
 */
@Composable
fun HourlyForecastCard(
    hourlyForecast: List<HourlyWeather>?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 标题
            Text(
                text = "24小时预报",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 小时预报列表
            hourlyForecast?.let { forecast ->
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(forecast.take(24)) { hourly ->
                        HourlyForecastItem(hourly = hourly)
                    }
                }
            } ?: run {
                // 空状态
                Text(
                    text = "暂无数据",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 单个小时预报项
 */
@Composable
fun HourlyForecastItem(
    hourly: HourlyWeather,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(60.dp)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 时间
        val timeText = formatHourlyTime(hourly.fxTime)
        val isNow = isCurrentHour(hourly.fxTime)
        
        Text(
            text = if (isNow) "现在" else timeText,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isNow) FontWeight.Bold else FontWeight.Normal,
            color = if (isNow) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.onSurfaceVariant
        )

        // 天气图标
        WeatherIcon(
            iconCode = hourly.icon ?: "100",
            isDay = isDayTime(hourly.fxTime),
            modifier = Modifier.size(32.dp)
        )

        // 温度
        Text(
            text = "${hourly.temp ?: "--"}°",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )

        // 降水概率
        hourly.pop?.let { pop ->
            if (pop.toIntOrNull()?.let { it > 0 } == true) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$pop%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/**
 * 格式化小时时间
 */
private fun formatHourlyTime(fxTime: String?): String {
    if (fxTime.isNullOrEmpty()) return "--"
    
    return try {
        // 和风天气时间格式: 2024-01-01T12:00+08:00
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mmXXX", Locale.getDefault())
        val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = inputFormat.parse(fxTime)
        date?.let { outputFormat.format(it) } ?: "--"
    } catch (e: Exception) {
        "--"
    }
}

/**
 * 判断是否为当前小时
 */
private fun isCurrentHour(fxTime: String?): Boolean {
    if (fxTime.isNullOrEmpty()) return false
    
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mmXXX", Locale.getDefault())
        val date = inputFormat.parse(fxTime)
        val now = Date()
        
        val hourFormat = SimpleDateFormat("yyyyMMddHH", Locale.getDefault())
        hourFormat.format(date) == hourFormat.format(now)
    } catch (e: Exception) {
        false
    }
}

/**
 * 判断是否为白天
 */
private fun isDayTime(fxTime: String?): Boolean {
    if (fxTime.isNullOrEmpty()) return true
    
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mmXXX", Locale.getDefault())
        val date = inputFormat.parse(fxTime)
        val hourFormat = SimpleDateFormat("HH", Locale.getDefault())
        val hour = date?.let { hourFormat.format(it).toInt() } ?: 12
        
        hour in 6..18
    } catch (e: Exception) {
        true
    }
}
