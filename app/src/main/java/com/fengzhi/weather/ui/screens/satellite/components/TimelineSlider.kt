package com.fengzhi.weather.ui.screens.satellite.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fengzhi.weather.data.model.TimelinePoint
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * 时间轴滑块组件
 * 用于选择不同时间点的卫星图像
 */
@Composable
fun TimelineSlider(
    timelinePoints: List<TimelinePoint>,
    currentIndex: Int,
    onTimeSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var sliderValue by remember { mutableFloatStateOf(currentIndex.toFloat()) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // 当前时间显示
        if (timelinePoints.isNotEmpty() && currentIndex in timelinePoints.indices) {
            Text(
                text = timelinePoints[currentIndex].displayTime,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        // 时间滑块
        Slider(
            value = sliderValue,
            onValueChange = { newValue ->
                sliderValue = newValue
                onValueChangeFinished(sliderValue.toInt(), timelinePoints, onTimeSelected)
            },
            valueRange = 0f..(timelinePoints.size - 1).coerceAtLeast(0).toFloat(),
            steps = (timelinePoints.size - 1).coerceAtLeast(0),
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )

        // 时间范围标签
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = timelinePoints.firstOrNull()?.displayTime ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = timelinePoints.lastOrNull()?.displayTime ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 紧凑型时间轴滑块
 */
@Composable
fun CompactTimelineSlider(
    timelinePoints: List<TimelinePoint>,
    currentIndex: Int,
    onTimeSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var sliderValue by remember(currentIndex) { mutableFloatStateOf(currentIndex.toFloat()) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 开始时间
        Text(
            text = formatCompactTime(timelinePoints.firstOrNull()?.millis),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 8.dp)
        )

        // 滑块
        Slider(
            value = sliderValue,
            onValueChange = { newValue ->
                sliderValue = newValue
            },
            onValueChangeFinished = {
                onValueChangeFinished(sliderValue.toInt(), timelinePoints, onTimeSelected)
            },
            valueRange = 0f..(timelinePoints.size - 1).coerceAtLeast(0).toFloat(),
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )

        // 结束时间
        Text(
            text = formatCompactTime(timelinePoints.lastOrNull()?.millis),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

/**
 * 时间轴指示器
 * 显示时间点标记
 */
@Composable
fun TimelineIndicator(
    timelinePoints: List<TimelinePoint>,
    currentIndex: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        timelinePoints.forEachIndexed { index, _ ->
            val isSelected = index == currentIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else Color.Transparent
                    )
            )
        }
    }
}

/**
 * 时间选择回调处理
 */
private fun onValueChangeFinished(
    newValue: Int,
    timelinePoints: List<TimelinePoint>,
    onTimeSelected: (Int) -> Unit
) {
    val clampedIndex = newValue.coerceIn(0, timelinePoints.size - 1)
    if (clampedIndex != newValue) return
    onTimeSelected(clampedIndex)
}

/**
 * 格式化紧凑时间显示
 */
private fun formatCompactTime(millis: Long?): String {
    if (millis == null) return ""
    val format = SimpleDateFormat("HH:mm", Locale.getDefault())
    return format.format(millis)
}
