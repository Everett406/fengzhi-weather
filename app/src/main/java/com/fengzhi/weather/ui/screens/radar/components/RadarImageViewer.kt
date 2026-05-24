package com.fengzhi.weather.ui.screens.radar.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toIntRect
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import com.fengzhi.weather.R
import com.fengzhi.weather.data.model.RadarFrame
import com.fengzhi.weather.data.model.RadarMapConfig
import com.fengzhi.weather.data.model.RadarState
import com.fengzhi.weather.data.model.RainViewerMetadata
import kotlin.math.roundToInt

/**
 * 雷达图像查看器
 * 显示雷达叠加图和地图背景
 */
@Composable
fun RadarImageViewer(
    state: RadarState.Success,
    modifier: Modifier = Modifier
) {
    val currentFrame = state.currentFrame
    val metadata = state.metadata
    
    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        // 地图背景
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1A1A2E))
        ) {
            // 简化的地图背景（网格）
            MapBackground(
                centerLat = state.centerLat,
                centerLon = state.centerLon,
                zoom = state.zoom,
                modifier = Modifier.fillMaxSize()
            )
            
            // 雷达瓦片叠加层
            if (currentFrame != null) {
                RadarTileOverlay(
                    metadata = metadata,
                    frame = currentFrame,
                    zoom = state.zoom,
                    centerLat = state.centerLat,
                    centerLon = state.centerLon,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        
        // 中心标记
        CenterMarker(
            modifier = Modifier.align(Alignment.Center)
        )
        
        // 坐标信息
        CoordinateInfo(
            lat = state.centerLat,
            lon = state.centerLon,
            zoom = state.zoom,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        )
    }
}

/**
 * 雷达瓦片叠加层
 */
@Composable
fun RadarTileOverlay(
    metadata: RainViewerMetadata,
    frame: RadarFrame,
    zoom: Int,
    centerLat: Double,
    centerLon: Double,
    modifier: Modifier = Modifier
) {
    val tileSize = RadarMapConfig.TILE_SIZE
    val density = LocalDensity.current
    
    // 计算中心瓦片坐标
    val centerTileX = lonToTileX(centerLon, zoom)
    val centerTileY = latToTileY(centerLat, zoom)
    
    var loadedCount by remember { mutableIntStateOf(0) }
    var hasStartedLoading by remember { mutableStateOf(false) }
    
    // 3x3 瓦片网格覆盖
    val tileRange = -1..1
    
    Box(modifier = modifier) {
        // 瓦片网格
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val tileSizePx = with(density) { tileSize.dp.roundToPx() }
            
            // 计算偏移量使中心对齐
            val centerOffsetX = (constraints.maxWidth - tileSizePx) / 2
            val centerOffsetY = (constraints.maxHeight - tileSizePx) / 2
            
            tileRange.forEach { dx ->
                tileRange.forEach { dy ->
                    val tileX = centerTileX + dx
                    val tileY = centerTileY + dy
                    
                    if (isValidTile(tileX, tileY, zoom)) {
                        val url = frame.getTileUrl(metadata.host, zoom, tileX, tileY)
                        
                        val offsetX = centerOffsetX + dx * tileSizePx
                        val offsetY = centerOffsetY + dy * tileSizePx
                        
                        AsyncImage(
                            model = url,
                            contentDescription = null,
                            modifier = Modifier
                                .offset { IntOffset(offsetX, offsetY) }
                                .size(tileSize.dp),
                            onState = { imageState ->
                                when (imageState) {
                                    is AsyncImagePainter.State.Success -> {
                                        loadedCount++
                                        hasStartedLoading = true
                                    }
                                    is AsyncImagePainter.State.Error -> {
                                        loadedCount++
                                        hasStartedLoading = true
                                    }
                                    is AsyncImagePainter.State.Loading -> {
                                        hasStartedLoading = true
                                    }
                                    else -> {}
                                }
                            },
                            contentScale = ContentScale.FillBounds
                        )
                    }
                }
            }
        }
    }
}

/**
 * 地图背景组件
 */
@Composable
fun MapBackground(
    centerLat: Double,
    centerLon: Double,
    zoom: Int,
    modifier: Modifier = Modifier
) {
    // 简化版地图背景 - 使用深色背景
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1E3A5F).copy(alpha = 0.3f))
    ) {
        // 可以在这里添加更复杂的地图背景
        // 例如 OpenStreetMap 底图或其他地图服务
    }
}

/**
 * 中心标记
 */
@Composable
fun CenterMarker(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(20.dp),
        contentAlignment = Alignment.Center
    ) {
        // 外圈
        Box(
            modifier = Modifier
                .size(20.dp)
                .alpha(0.3f)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
        )
        // 内圈
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
        )
    }
}

/**
 * 坐标信息显示
 */
@Composable
fun CoordinateInfo(
    lat: Double,
    lon: Double,
    zoom: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(8.dp)
    ) {
        Text(
            text = String.format("纬度: %.2f°", lat),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = String.format("经度: %.2f°", lon),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "缩放: $zoom",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 简化版雷达图像（单瓦片）
 */
@Composable
fun SimpleRadarImage(
    metadata: RainViewerMetadata,
    frame: RadarFrame,
    zoom: Int,
    modifier: Modifier = Modifier
) {
    val centerTileX = lonToTileX(RadarMapConfig.DEFAULT_CENTER_LON, zoom)
    val centerTileY = latToTileY(RadarMapConfig.DEFAULT_CENTER_LAT, zoom)
    val url = frame.getTileUrl(metadata.host, zoom, centerTileX, centerTileY)
    
    var imageState by remember { mutableStateOf<AsyncImagePainter.State>(AsyncImagePainter.State.Empty) }
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        when (imageState) {
            is AsyncImagePainter.State.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            is AsyncImagePainter.State.Error -> {
                Text(
                    text = stringResource(R.string.radar_image_load_failed),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            else -> {}
        }
        
        AsyncImage(
            model = url,
            contentDescription = stringResource(R.string.radar_image),
            modifier = Modifier.fillMaxSize(),
            onState = { imageState = it },
            contentScale = ContentScale.Fit
        )
    }
}

// 辅助函数
private fun lonToTileX(lon: Double, zoom: Int): Int {
    return ((lon + 180) / 360 * (1 shl zoom)).toInt()
}

private fun latToTileY(lat: Double, zoom: Int): Int {
    val latRad = Math.toRadians(lat)
    return ((1 - Math.log(Math.tan(latRad) + 1 / Math.cos(latRad)) / Math.PI) / 2 * (1 shl zoom)).toInt()
}

private fun isValidTile(x: Int, y: Int, zoom: Int): Boolean {
    val max = 1 shl zoom
    return x in 0 until max && y in 0 until max
}
