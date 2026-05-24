package com.fengzhi.weather.ui.screens.satellite.components

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.fengzhi.weather.data.model.SatelliteBand
import com.fengzhi.weather.data.model.SatelliteTile
import com.fengzhi.weather.data.model.TileLoadState

/**
 * 卫星图像查看器
 * 支持缩放和平移手势
 */
@Composable
fun SatelliteImageViewer(
    tiles: List<SatelliteTile>,
    scale: Int,
    band: SatelliteBand,
    tileLoadStates: Map<String, TileLoadState>,
    onTileClick: (Int, Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var zoomScale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    zoomScale = (zoomScale * zoom).coerceIn(0.5f, 5f)
                    
                    // 限制偏移范围
                    val maxOffsetX = (size.width * (zoomScale - 1)) / 2
                    val maxOffsetY = (size.height * (zoomScale - 1)) / 2
                    
                    offsetX = (offsetX + pan.x * zoomScale).coerceIn(-maxOffsetX, maxOffsetX)
                    offsetY = (offsetY + pan.y * zoomScale).coerceIn(-maxOffsetY, maxOffsetY)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        // 双击重置缩放
                        if (zoomScale != 1f) {
                            zoomScale = 1f
                            offsetX = 0f
                            offsetY = 0f
                        } else {
                            zoomScale = 2f
                        }
                    }
                )
            }
    ) {
        // 瓦片网格
        TileGrid(
            tiles = tiles,
            scale = scale,
            tileLoadStates = tileLoadStates,
            zoomScale = zoomScale,
            offsetX = offsetX,
            offsetY = offsetY,
            onTileClick = onTileClick,
            modifier = Modifier.align(Alignment.Center)
        )

        // 缩放指示器
        if (zoomScale != 1f) {
            ZoomIndicator(
                scale = zoomScale,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            )
        }
    }
}

/**
 * 瓦片网格
 */
@Composable
private fun TileGrid(
    tiles: List<SatelliteTile>,
    scale: Int,
    tileLoadStates: Map<String, TileLoadState>,
    zoomScale: Float,
    offsetX: Float,
    offsetY: Float,
    onTileClick: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val tileSize = 550 // 瓦片像素大小

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = zoomScale
                scaleY = zoomScale
                translationX = offsetX
                translationY = offsetY
            }
    ) {
        Column {
            for (y in 0 until scale) {
                Row {
                    for (x in 0 until scale) {
                        val tile = tiles.find { it.x == x && it.y == y }
                        val loadState = tile?.let { 
                            tileLoadStates["${it.x}_${it.y}"] 
                        } ?: TileLoadState(x, y)

                        TileItem(
                            tile = tile,
                            loadState = loadState,
                            tileSize = tileSize,
                            onClick = { onTileClick(x, y) },
                            modifier = Modifier
                                .width((tileSize / 2).dp)
                                .height((tileSize / 2).dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 单个瓦片项
 */
@Composable
private fun TileItem(
    tile: SatelliteTile?,
    loadState: TileLoadState,
    tileSize: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color(0xFF1a1a2e))
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onClick() })
            },
        contentAlignment = Alignment.Center
    ) {
        if (tile != null) {
            AsyncImage(
                model = tile.url,
                contentDescription = "卫星图像瓦片 ${tile.x}_${tile.y}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // 加载中状态
            if (loadState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp
                )
            }

            // 错误状态
            if (loadState.isError) {
                Text(
                    text = "加载失败",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        } else {
            // 占位符
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF16213e)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp
                )
            }
        }
    }
}

/**
 * 缩放指示器
 */
@Composable
private fun ZoomIndicator(
    scale: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                shape = MaterialTheme.shapes.small
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = String.format("%.1fx", scale),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * 合成图像查看器
 * 用于显示已经合成的完整图像
 */
@Composable
fun ComposedImageViewer(
    bitmap: Bitmap?,
    isLoading: Boolean,
    error: String?,
    modifier: Modifier = Modifier
) {
    var zoomScale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    zoomScale = (zoomScale * zoom).coerceIn(0.5f, 5f)
                    val maxOffsetX = (size.width * (zoomScale - 1)) / 2
                    val maxOffsetY = (size.height * (zoomScale - 1)) / 2
                    offsetX = (offsetX + pan.x * zoomScale).coerceIn(-maxOffsetX, maxOffsetX)
                    offsetY = (offsetY + pan.y * zoomScale).coerceIn(-maxOffsetY, maxOffsetY)
                }
            }
    ) {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "加载卫星图像中...",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            bitmap != null -> {
                // 显示合成图像
                AsyncImage(
                    model = bitmap,
                    contentDescription = "卫星云图",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = zoomScale
                            scaleY = zoomScale
                            translationX = offsetX
                            translationY = offsetY
                        },
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}
