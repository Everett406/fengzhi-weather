package com.fengzhi.weather.ui.screens.radar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fengzhi.weather.R
import com.fengzhi.weather.data.model.PrecipitationLevel

/**
 * 雷达图例组件
 * 显示降水强度颜色图例
 */
@Composable
fun RadarLegend(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = stringResource(R.string.precipitation_intensity),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 颜色条
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                PrecipitationLevel.values().forEach { level ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(12.dp)
                            .background(
                                color = parseColor(level.colorHex),
                                shape = RoundedCornerShape(1.dp)
                            )
                    )
                }
            }
        }
        
        // 标签
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.light_rain),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            )
            Text(
                text = stringResource(R.string.heavy_rain),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            )
            Text(
                text = stringResource(R.string.hail),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            )
        }
    }
}

/**
 * 解析十六进制颜色字符串
 */
private fun parseColor(colorHex: String): Color {
    return try {
        val hex = colorHex.removePrefix("#")
        val color = hex.toLong(16)
        when (hex.length) {
            6 -> Color(0xFF000000 or color)
            8 -> Color(color)
            else -> Color.Gray
        }
    } catch (e: Exception) {
        Color.Gray
    }
}

/**
 * 简化版图例（仅显示关键级别）
 */
@Composable
fun CompactRadarLegend(
    modifier: Modifier = Modifier
) {
    val keyLevels = listOf(
        PrecipitationLevel.LIGHT to "小雨",
        PrecipitationLevel.MODERATE to "中雨",
        PrecipitationLevel.HEAVY to "大雨",
        PrecipitationLevel.VERY_HEAVY to "暴雨",
        PrecipitationLevel.EXTREME to "特大暴雨"
    )
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        keyLevels.forEach { (level, label) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(12.dp)
                        .height(12.dp)
                        .background(
                            color = parseColor(level.colorHex),
                            shape = RoundedCornerShape(2.dp)
                        )
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            }
        }
    }
}
