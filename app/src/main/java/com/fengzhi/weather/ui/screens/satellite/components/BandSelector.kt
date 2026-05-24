package com.fengzhi.weather.ui.screens.satellite.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fengzhi.weather.data.model.SatelliteBand

/**
 * 波段选择器组件
 * 用于切换可见光、红外、水汽三种波段
 */
@Composable
fun BandSelector(
    currentBand: SatelliteBand,
    onBandSelected: (SatelliteBand) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "波段选择",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SatelliteBand.values().forEach { band ->
                BandChip(
                    band = band,
                    isSelected = currentBand == band,
                    onClick = { onBandSelected(band) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * 波段选择芯片
 */
@Composable
private fun BandChip(
    band: SatelliteBand,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, label, description) = when (band) {
        SatelliteBand.VISIBLE_LIGHT -> Triple(
            Icons.Filled.Visibility,
            "可见光",
            "真彩色"
        )
        SatelliteBand.INFRARED -> Triple(
            Icons.Filled.Visibility, // 使用相同图标，但颜色不同
            "红外",
            "全天候"
        )
        SatelliteBand.WATER_VAPOR -> Triple(
            Icons.Filled.WaterDrop,
            "水汽",
            "水汽分布"
        )
    }

    val backgroundColor = if (isSelected) {
        when (band) {
            SatelliteBand.VISIBLE_LIGHT -> Color(0xFF4CAF50).copy(alpha = 0.2f)
            SatelliteBand.INFRARED -> Color(0xFFFF5722).copy(alpha = 0.2f)
            SatelliteBand.WATER_VAPOR -> Color(0xFF2196F3).copy(alpha = 0.2f)
        }
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val borderColor = if (isSelected) {
        when (band) {
            SatelliteBand.VISIBLE_LIGHT -> Color(0xFF4CAF50)
            SatelliteBand.INFRARED -> Color(0xFFFF5722)
            SatelliteBand.WATER_VAPOR -> Color(0xFF2196F3)
        }
    } else {
        Color.Transparent
    }

    val textColor = if (isSelected) {
        when (band) {
            SatelliteBand.VISIBLE_LIGHT -> Color(0xFF4CAF50)
            SatelliteBand.INFRARED -> Color(0xFFFF5722)
            SatelliteBand.WATER_VAPOR -> Color(0xFF2196F3)
        }
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = textColor
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor.copy(alpha = 0.7f)
                )
            }
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = textColor,
                modifier = Modifier.size(18.dp)
            )
        },
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            selectedContainerColor = backgroundColor
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = MaterialTheme.colorScheme.outline,
            selectedBorderColor = borderColor,
            enabled = true,
            selected = isSelected
        )
    )
}

/**
 * 紧凑型波段选择器
 */
@Composable
fun CompactBandSelector(
    currentBand: SatelliteBand,
    onBandSelected: (SatelliteBand) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        SatelliteBand.values().forEach { band ->
            val isSelected = currentBand == band
            val backgroundColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                Color.Transparent
            }

            val textColor = if (isSelected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(backgroundColor)
                    .clickable { onBandSelected(band) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = band.displayName,
                    style = MaterialTheme.typography.labelMedium,
                    color = textColor,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * 圆形波段选择器
 */
@Composable
fun CircleBandSelector(
    currentBand: SatelliteBand,
    onBandSelected: (SatelliteBand) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SatelliteBand.values().forEach { band ->
            val isSelected = currentBand == band
            
            val color = when (band) {
                SatelliteBand.VISIBLE_LIGHT -> Color(0xFF4CAF50)
                SatelliteBand.INFRARED -> Color(0xFFFF5722)
                SatelliteBand.WATER_VAPOR -> Color(0xFF2196F3)
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) color.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = if (isSelected) color else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { onBandSelected(band) },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                }

                Text(
                    text = band.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
