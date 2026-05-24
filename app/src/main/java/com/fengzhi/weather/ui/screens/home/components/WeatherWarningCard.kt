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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fengzhi.weather.data.model.WeatherWarning
import com.fengzhi.weather.utils.Constants

/**
 * 天气预警卡片
 * 显示当前地区的天气预警信息
 */
@Composable
fun WeatherWarningCard(
    warnings: List<WeatherWarning>?,
    modifier: Modifier = Modifier
) {
    if (warnings.isNullOrEmpty()) {
        // 无预警时不显示
        return
    }

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
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "天气预警",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${warnings.size}条",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 预警列表
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height((warnings.size * 80).dp.coerceAtMost(240.dp))
            ) {
                items(warnings) { warning ->
                    WarningItem(warning = warning)
                }
            }
        }
    }
}

/**
 * 单条预警项
 */
@Composable
fun WarningItem(
    warning: WeatherWarning,
    modifier: Modifier = Modifier
) {
    val warningColor = getWarningColor(warning.level)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(warningColor.copy(alpha = 0.1f))
            .padding(12.dp)
    ) {
        // 预警等级标签
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(warningColor)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = warning.level ?: "预警",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 预警内容
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // 预警标题
            Text(
                text = warning.title ?: warning.typeName ?: "天气预警",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 预警详情
            warning.text?.let { text ->
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 发布时间
            warning.pubTime?.let { time ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatWarningTime(time),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 获取预警等级颜色
 */
private fun getWarningColor(level: String?): Color {
    return when (level) {
        Constants.WarningLevel.RED -> Color(0xFFE53935)
        Constants.WarningLevel.ORANGE -> Color(0xFFFF9800)
        Constants.WarningLevel.YELLOW -> Color(0xFFFDD835)
        Constants.WarningLevel.BLUE -> Color(0xFF1E88E5)
        Constants.WarningLevel.WHITE -> Color(0xFF9E9E9E)
        else -> Color(0xFFFF9800)
    }
}

/**
 * 格式化预警时间
 */
private fun formatWarningTime(time: String?): String {
    if (time.isNullOrEmpty()) return ""
    
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mmXXX", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
        val date = inputFormat.parse(time)
        date?.let { outputFormat.format(it) } ?: ""
    } catch (e: Exception) {
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
            val date = inputFormat.parse(time)
            date?.let { outputFormat.format(it) } ?: ""
        } catch (e: Exception) {
            ""
        }
    }
}
