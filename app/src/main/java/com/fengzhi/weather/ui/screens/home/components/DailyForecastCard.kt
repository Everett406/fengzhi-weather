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
import androidx.compose.foundation.lazy.LazyColumn
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
import com.fengzhi.weather.data.model.DailyWeather
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 7天预报卡片
 * 显示未来7天天气预报列表
 */
@Composable
fun DailyForecastCard(
    dailyForecast: List<DailyWeather>?,
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
                text = "7天预报",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 每日预报列表
            dailyForecast?.let { forecast ->
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.height((forecast.size * 56).dp.coerceAtMost(400.dp))
                ) {
                    items(forecast.take(7)) { daily ->
                        DailyForecastItem(daily = daily)
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
 * 单日预报项
 */
@Composable
fun DailyForecastItem(
    daily: DailyWeather,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 日期
        Column(
            modifier = Modifier.weight(0.8f)
        ) {
            val dateText = formatDailyDate(daily.fxDate)
            val dayOfWeek = formatDayOfWeek(daily.fxDate)
            val isToday = isToday(daily.fxDate)
            
            Text(
                text = if (isToday) "今天" else dayOfWeek,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = dateText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 白天天气图标和状况
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            WeatherIcon(
                iconCode = daily.iconDay ?: "100",
                isDay = true,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = daily.textDay ?: "--",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 温度范围
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.End
        ) {
            // 最低温度
            Text(
                text = "${daily.tempMin ?: "--"}°",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = " / ",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // 最高温度
            Text(
                text = "${daily.tempMax ?: "--"}°",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // 夜间天气图标
        Spacer(modifier = Modifier.width(8.dp))
        WeatherIcon(
            iconCode = daily.iconNight ?: "150",
            isDay = false,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * 格式化日期
 */
private fun formatDailyDate(fxDate: String?): String {
    if (fxDate.isNullOrEmpty()) return "--"
    
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MM/dd", Locale.getDefault())
        val date = inputFormat.parse(fxDate)
        date?.let { outputFormat.format(it) } ?: "--"
    } catch (e: Exception) {
        "--"
    }
}

/**
 * 格式化星期几
 */
private fun formatDayOfWeek(fxDate: String?): String {
    if (fxDate.isNullOrEmpty()) return "--"
    
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("EEEE", Locale.CHINA)
        val date = inputFormat.parse(fxDate)
        date?.let { outputFormat.format(it) } ?: "--"
    } catch (e: Exception) {
        "--"
    }
}

/**
 * 判断是否为今天
 */
private fun isToday(fxDate: String?): Boolean {
    if (fxDate.isNullOrEmpty()) return false
    
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = inputFormat.parse(fxDate)
        val today = inputFormat.parse(inputFormat.format(Date()))
        date == today
    } catch (e: Exception) {
        false
    }
}
