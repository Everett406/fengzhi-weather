package com.fengzhi.weather.ui.screens.radar.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fengzhi.weather.R
import com.fengzhi.weather.data.model.RadarFrame
import com.fengzhi.weather.data.model.RadarState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 雷达时间线组件
 * 显示时间轴滑块和播放控制
 */
@Composable
fun RadarTimeline(
    state: RadarState.Success,
    onFrameChange: (Int) -> Unit,
    onPlayPauseToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentFrame = state.currentFrame
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // 时间显示和播放控制
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 当前时间
            Text(
                text = currentFrame?.let { formatFrameTime(it) } ?: "--:--",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // 播放/暂停按钮
            IconButton(onClick = onPlayPauseToggle) {
                Icon(
                    imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (state.isPlaying) {
                        stringResource(R.string.pause)
                    } else {
                        stringResource(R.string.play)
                    },
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            // 帧数信息
            Text(
                text = "${state.currentFrameIndex + 1}/${state.totalFrames}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // 时间轴滑块
        Slider(
            value = state.currentFrameIndex.toFloat(),
            onValueChange = { onFrameChange(it.toInt()) },
            valueRange = 0f..(state.totalFrames - 1).toFloat().coerceAtLeast(0f),
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
                text = state.allFrames.firstOrNull()?.let { formatFrameTime(it) } ?: "--:--",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // 过去/预报分界标记
            val nowcastStartIndex = state.allFrames.indexOfFirst { it.isNowcast() }
            if (nowcastStartIndex >= 0) {
                Text(
                    text = stringResource(R.string.forecast_start),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            
            Text(
                text = state.allFrames.lastOrNull()?.let { formatFrameTime(it) } ?: "--:--",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // 过去/预报标签
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 过去数据标签
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .height(8.dp)
                        .weight(0.5f)
                        .padding(end = 4.dp)
                )
                Text(
                    text = stringResource(R.string.past_2_hours),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 预报数据标签
            if (state.nowcastFrames.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.forecast_2_hours),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

/**
 * 格式化帧时间显示
 */
private fun formatFrameTime(frame: RadarFrame): String {
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    return dateFormat.format(Date(frame.time * 1000))
}

/**
 * 紧凑版时间线（仅显示进度条）
 */
@Composable
fun CompactRadarTimeline(
    progress: Float,
    onProgressChange: (Float) -> Unit,
    isPlaying: Boolean,
    onPlayPauseToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(onClick = onPlayPauseToggle) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        Slider(
            value = progress,
            onValueChange = onProgressChange,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}
