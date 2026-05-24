package com.fengzhi.weather.ui.screens.satellite

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SatelliteAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fengzhi.weather.data.model.AnimationSpeed
import com.fengzhi.weather.data.model.SatelliteBand
import com.fengzhi.weather.data.model.SatelliteUiState
import com.fengzhi.weather.ui.screens.satellite.components.AnimationControls
import com.fengzhi.weather.ui.screens.satellite.components.BandSelector
import com.fengzhi.weather.ui.screens.satellite.components.SatelliteImageViewer
import com.fengzhi.weather.ui.screens.satellite.components.TimelineSlider

/**
 * 卫星云图主界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SatelliteScreen(
    viewModel: SatelliteViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val tileLoadStates by viewModel.tileLoadStates.collectAsState()
    val currentTimeIndex by viewModel.currentTimeIndex.collectAsState()
    val currentBand by viewModel.currentBand.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val animationSpeed by viewModel.animationSpeed.collectAsState()
    val timelinePoints by viewModel.timelinePoints.collectAsState()
    val currentImage by viewModel.currentImage.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SatelliteAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "卫星云图",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "刷新",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                modifier = Modifier.statusBarsPadding()
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is SatelliteUiState.Loading -> {
                    LoadingContent(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    )
                }

                is SatelliteUiState.Error -> {
                    ErrorContent(
                        message = state.message,
                        onRetry = { viewModel.refresh() },
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    )
                }

                is SatelliteUiState.Success -> {
                    // 卫星图像查看器
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        currentImage?.let { image ->
                            SatelliteImageViewer(
                                tiles = image.tiles,
                                scale = image.scale,
                                band = currentBand,
                                tileLoadStates = tileLoadStates,
                                onTileClick = { x, y ->
                                    viewModel.retryTile(x, y)
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // 时间显示
                        if (timelinePoints.isNotEmpty() && currentTimeIndex in timelinePoints.indices) {
                            TimeOverlay(
                                displayTime = timelinePoints[currentTimeIndex].displayTime,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(16.dp)
                            )
                        }
                    }

                    // 控制面板
                    ControlPanel(
                        currentBand = currentBand,
                        onBandSelected = { viewModel.selectBand(it) },
                        timelinePoints = timelinePoints,
                        currentTimeIndex = currentTimeIndex,
                        onTimeSelected = { viewModel.selectTime(it) },
                        isPlaying = isPlaying,
                        animationSpeed = animationSpeed,
                        onPlayPauseClick = { viewModel.togglePlayPause() },
                        onSpeedChange = { viewModel.setAnimationSpeed(it) },
                        onPreviousClick = { viewModel.previousFrame() },
                        onNextClick = { viewModel.nextFrame() },
                        canGoPrevious = currentTimeIndex > 0,
                        canGoNext = currentTimeIndex < timelinePoints.size - 1
                    )
                }
            }
        }
    }
}

/**
 * 加载中内容
 */
@Composable
private fun LoadingContent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "正在加载卫星云图...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 错误内容
 */
@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.SatelliteAlt,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            Button(onClick = onRetry) {
                Text(text = "重试")
            }
        }
    }
}

/**
 * 时间覆盖层
 */
@Composable
private fun TimeOverlay(
    displayTime: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Text(
            text = displayTime,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

/**
 * 控制面板
 */
@Composable
private fun ControlPanel(
    currentBand: SatelliteBand,
    onBandSelected: (SatelliteBand) -> Unit,
    timelinePoints: List<com.fengzhi.weather.data.model.TimelinePoint>,
    currentTimeIndex: Int,
    onTimeSelected: (Int) -> Unit,
    isPlaying: Boolean,
    animationSpeed: AnimationSpeed,
    onPlayPauseClick: () -> Unit,
    onSpeedChange: (AnimationSpeed) -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    canGoPrevious: Boolean,
    canGoNext: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 波段选择器
            BandSelector(
                currentBand = currentBand,
                onBandSelected = onBandSelected,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 时间轴滑块
            if (timelinePoints.isNotEmpty()) {
                TimelineSlider(
                    timelinePoints = timelinePoints,
                    currentIndex = currentTimeIndex,
                    onTimeSelected = onTimeSelected,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 动画控制
            AnimationControls(
                isPlaying = isPlaying,
                currentSpeed = animationSpeed,
                onPlayPauseClick = onPlayPauseClick,
                onSpeedChange = onSpeedChange,
                onPreviousClick = onPreviousClick,
                onNextClick = onNextClick,
                canGoPrevious = canGoPrevious,
                canGoNext = canGoNext,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * 卫星云图预览组件
 * 用于在其他页面显示小型预览
 */
@Composable
fun SatellitePreview(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.SatelliteAlt,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "卫星云图",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "查看实时卫星云图",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
