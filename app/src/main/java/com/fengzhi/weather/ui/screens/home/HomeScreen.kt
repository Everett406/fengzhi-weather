package com.fengzhi.weather.ui.screens.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fengzhi.weather.R
import com.fengzhi.weather.data.model.HomeWeatherData
import com.fengzhi.weather.data.model.UiState
import com.fengzhi.weather.ui.screens.home.components.AirQualityCard
import com.fengzhi.weather.ui.screens.home.components.AirQualitySkeleton
import com.fengzhi.weather.ui.screens.home.components.CurrentWeatherCard
import com.fengzhi.weather.ui.screens.home.components.CurrentWeatherSkeleton
import com.fengzhi.weather.ui.screens.home.components.DailyForecastCard
import com.fengzhi.weather.ui.screens.home.components.DailyForecastSkeleton
import com.fengzhi.weather.ui.screens.home.components.HourlyForecastCard
import com.fengzhi.weather.ui.screens.home.components.HourlyForecastSkeleton
import com.fengzhi.weather.ui.screens.home.components.WeatherWarningCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit = {},
    onNavigateToCitySearch: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val swipeRefreshState = rememberSwipeRefreshState(
        isRefreshing = uiState.isRefreshing
    )

    Scaffold(
        topBar = {
            HomeTopAppBar(
                locationName = uiState.locationName,
                lastUpdateTime = uiState.lastUpdateTime,
                onSettingsClick = onNavigateToSettings,
                onRefreshClick = { viewModel.refresh() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCitySearch,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "添加城市"
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 主内容
            AnimatedContent(
                targetState = uiState.weatherState,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith
                            fadeOut(animationSpec = tween(300))
                },
                label = "weather_content_animation"
            ) { state ->
                when (state) {
                    is UiState.Loading -> {
                        LoadingContent()
                    }
                    is UiState.Success -> {
                        WeatherContent(
                            data = state.data,
                            locationName = uiState.locationName
                        )
                    }
                    is UiState.Error -> {
                        ErrorContent(
                            message = state.message,
                            onRetry = { viewModel.retry() }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopAppBar(
    locationName: String,
    lastUpdateTime: String?,
    onSettingsClick: () -> Unit,
    onRefreshClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = locationName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                lastUpdateTime?.let { time ->
                    Text(
                        text = " · $time",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = onRefreshClick) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = "刷新"
                )
            }
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = "设置"
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}

/**
 * 加载中内容
 */
@Composable
fun LoadingContent(
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            CurrentWeatherSkeleton()
        }
        item {
            HourlyForecastSkeleton()
        }
        item {
            DailyForecastSkeleton()
        }
        item {
            AirQualitySkeleton()
        }
    }
}

/**
 * 天气内容
 */
@Composable
fun WeatherContent(
    data: HomeWeatherData,
    locationName: String,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // 当前天气卡片
        item {
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(
                    animationSpec = tween(300),
                    initialOffsetY = { -it }
                ) + fadeIn(),
                exit = fadeOut()
            ) {
                CurrentWeatherCard(
                    weather = data.currentWeather,
                    locationName = locationName
                )
            }
        }

        // 天气预警卡片（如果有预警）
        data.warnings?.let { warnings ->
            if (warnings.isNotEmpty()) {
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically(
                            animationSpec = tween(300, delayMillis = 100),
                            initialOffsetY = { it }
                        ) + fadeIn(),
                        exit = fadeOut()
                    ) {
                        WeatherWarningCard(warnings = warnings)
                    }
                }
            }
        }

        // 24小时预报卡片
        item {
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(
                    animationSpec = tween(300, delayMillis = 150),
                    initialOffsetY = { it }
                ) + fadeIn(),
                exit = fadeOut()
            ) {
                HourlyForecastCard(hourlyForecast = data.hourlyForecast)
            }
        }

        // 7天预报卡片
        item {
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(
                    animationSpec = tween(300, delayMillis = 200),
                    initialOffsetY = { it }
                ) + fadeIn(),
                exit = fadeOut()
            ) {
                DailyForecastCard(dailyForecast = data.dailyForecast)
            }
        }

        // 空气质量卡片
        item {
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(
                    animationSpec = tween(300, delayMillis = 250),
                    initialOffsetY = { it }
                ) + fadeIn(),
                exit = fadeOut()
            ) {
                AirQualityCard(airQuality = data.airQuality)
            }
        }

        // 底部间距
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

/**
 * 错误内容
 */
@Composable
fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(64.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "加载失败",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                TextButton(
                    onClick = onRetry,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(text = "重试")
                }
            }
        }
    }
}
