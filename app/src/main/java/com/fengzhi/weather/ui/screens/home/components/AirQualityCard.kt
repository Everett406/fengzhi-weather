package com.fengzhi.weather.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fengzhi.weather.data.model.AirQualityNow
import com.fengzhi.weather.utils.Constants
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * 空气质量卡片
 * 显示 AQI 指数和各项污染物数据
 */
@Composable
fun AirQualityCard(
    airQuality: AirQualityNow?,
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
            // 标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "空气质量",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // 更新时间
                airQuality?.pubTime?.let { time ->
                    Text(
                        text = formatUpdateTime(time),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            airQuality?.let { aqi ->
                // AQI 主要指标
                val aqiValue = aqi.aqi?.toIntOrNull() ?: 0
                val aqiLevel = Constants.AqiLevel.getLevel(aqiValue)
                val aqiColor = getAqiColor(aqiValue)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // AQI 数值圆环
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // 简化的 AQI 显示
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = aqiValue.toString(),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = aqiColor
                            )
                            Text(
                                text = aqiLevel,
                                style = MaterialTheme.typography.labelSmall,
                                color = aqiColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // 主要污染物
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "主要污染物",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = aqi.primary ?: "--",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // AQI 等级条
                AqiLevelBar(currentAqi = aqiValue)

                Spacer(modifier = Modifier.height(16.dp))

                // 污染物详情
                PollutantGrid(airQuality = aqi)
            } ?: run {
                // 空状态
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无数据",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * AQI 等级条
 */
@Composable
fun AqiLevelBar(
    currentAqi: Int,
    modifier: Modifier = Modifier
) {
    val levels = listOf(
        50 to "优",
        100 to "良",
        150 to "轻度",
        200 to "中度",
        300 to "重度",
        500 to "严重"
    )

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // 等级条
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        ) {
            levels.forEachIndexed { index, (maxValue, _) ->
                val color = when (index) {
                    0 -> Color(0xFF4CAF50)  // 优 - 绿色
                    1 -> Color(0xFFCDDC39)  // 良 - 黄绿色
                    2 -> Color(0xFFFF9800)  // 轻度 - 橙色
                    3 -> Color(0xFFF44336)  // 中度 - 红色
                    4 -> Color(0xFF9C27B0)  // 重度 - 紫色
                    else -> Color(0xFF795548) // 严重 - 褐红色
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(color)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 等级标签
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            levels.forEach { (_, label) ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * 污染物网格
 */
@Composable
fun PollutantGrid(
    airQuality: AirQualityNow,
    modifier: Modifier = Modifier
) {
    val pollutants = listOf(
        "PM2.5" to airQuality.pm2p5,
        "PM10" to airQuality.pm10,
        "O₃" to airQuality.o3,
        "NO₂" to airQuality.no2,
        "SO₂" to airQuality.so2,
        "CO" to airQuality.co
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 第一行
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            pollutants.take(3).forEach { (name, value) ->
                PollutantItem(
                    name = name,
                    value = value,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 第二行
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            pollutants.drop(3).forEach { (name, value) ->
                PollutantItem(
                    name = name,
                    value = value,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * 单个污染物项
 */
@Composable
fun PollutantItem(
    name: String,
    value: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value ?: "--",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 获取 AQI 颜色
 */
private fun getAqiColor(aqi: Int): Color {
    return when {
        aqi <= 50 -> Color(0xFF4CAF50)
        aqi <= 100 -> Color(0xFFCDDC39)
        aqi <= 150 -> Color(0xFFFF9800)
        aqi <= 200 -> Color(0xFFF44336)
        aqi <= 300 -> Color(0xFF9C27B0)
        else -> Color(0xFF795548)
    }
}

/**
 * 格式化更新时间
 */
private fun formatUpdateTime(time: String?): String {
    if (time.isNullOrEmpty()) return ""
    
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mmXXX", Locale.getDefault())
        val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = inputFormat.parse(time)
        date?.let { "更新于 ${outputFormat.format(it)}" } ?: ""
    } catch (e: Exception) {
        ""
    }
}
