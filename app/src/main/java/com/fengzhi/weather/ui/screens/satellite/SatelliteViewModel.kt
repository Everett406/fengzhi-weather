package com.fengzhi.weather.ui.screens.satellite

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fengzhi.weather.data.api.NictApi
import com.fengzhi.weather.data.model.AnimationSpeed
import com.fengzhi.weather.data.model.ParsedTimestamp
import com.fengzhi.weather.data.model.SatelliteBand
import com.fengzhi.weather.data.model.SatelliteImage
import com.fengzhi.weather.data.model.SatelliteTile
import com.fengzhi.weather.data.model.SatelliteUiState
import com.fengzhi.weather.data.model.TileLoadState
import com.fengzhi.weather.data.model.TimelinePoint
import com.fengzhi.weather.data.repository.SatelliteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 卫星云图 ViewModel
 * 管理卫星图像的状态和动画播放
 */
@HiltViewModel
class SatelliteViewModel @Inject constructor(
    private val satelliteRepository: SatelliteRepository
) : ViewModel() {

    // UI 状态
    private val _uiState = MutableStateFlow<SatelliteUiState>(SatelliteUiState.Loading)
    val uiState: StateFlow<SatelliteUiState> = _uiState.asStateFlow()

    // 瓦片加载状态
    private val _tileLoadStates = MutableStateFlow<Map<String, TileLoadState>>(emptyMap())
    val tileLoadStates: StateFlow<Map<String, TileLoadState>> = _tileLoadStates.asStateFlow()

    // 当前时间索引
    private val _currentTimeIndex = MutableStateFlow(0)
    val currentTimeIndex: StateFlow<Int> = _currentTimeIndex.asStateFlow()

    // 当前波段
    private val _currentBand = MutableStateFlow(SatelliteBand.VISIBLE_LIGHT)
    val currentBand: StateFlow<SatelliteBand> = _currentBand.asStateFlow()

    // 是否正在播放动画
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    // 动画速度
    private val _animationSpeed = MutableStateFlow(AnimationSpeed.NORMAL)
    val animationSpeed: StateFlow<AnimationSpeed> = _animationSpeed.asStateFlow()

    // 时间轴点列表
    private val _timelinePoints = MutableStateFlow<List<TimelinePoint>>(emptyList())
    val timelinePoints: StateFlow<List<TimelinePoint>> = _timelinePoints.asStateFlow()

    // 当前卫星图像
    private val _currentImage = MutableStateFlow<SatelliteImage?>(null)
    val currentImage: StateFlow<SatelliteImage?> = _currentImage.asStateFlow()

    // 合成的位图图像
    private val _composedBitmap = MutableStateFlow<Bitmap?>(null)
    val composedBitmap: StateFlow<Bitmap?> = _composedBitmap.asStateFlow()

    // 动画播放任务
    private var animationJob: Job? = null

    // 缩放级别
    private val _scale = MutableStateFlow(NictApi.DEFAULT_SCALE)

    init {
        loadInitialData()
    }

    /**
     * 加载初始数据
     */
    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = SatelliteUiState.Loading

            satelliteRepository.getLatestTimestamp()
                .onSuccess { timestamp ->
                    val parsedTimestamp = timestamp.parseTimestamp()
                    if (parsedTimestamp != null) {
                        // 生成时间轴
                        val timeline = satelliteRepository.generateTimelinePoints(parsedTimestamp)
                        _timelinePoints.value = timeline
                        _currentTimeIndex.value = timeline.size - 1 // 设置为最新时间

                        // 加载当前图像
                        loadSatelliteImage(
                            timestamp = timeline.last().timestamp,
                            band = _currentBand.value,
                            scale = _scale.value
                        )
                    } else {
                        _uiState.value = SatelliteUiState.Error("解析时间戳失败")
                    }
                }
                .onFailure { error ->
                    _uiState.value = SatelliteUiState.Error(
                        error.message ?: "获取卫星数据失败"
                    )
                }
        }
    }

    /**
     * 加载卫星图像
     */
    private suspend fun loadSatelliteImage(
        timestamp: ParsedTimestamp,
        band: SatelliteBand,
        scale: Int
    ) {
        _uiState.value = SatelliteUiState.Loading

        // 初始化瓦片加载状态
        val initialLoadStates = mutableMapOf<String, TileLoadState>()
        for (x in 0 until scale) {
            for (y in 0 until scale) {
                initialLoadStates["${x}_${y}"] = TileLoadState(x, y, isLoading = true)
            }
        }
        _tileLoadStates.value = initialLoadStates

        satelliteRepository.getSatelliteImage(timestamp, band, scale)
            .onSuccess { image ->
                _currentImage.value = image
                
                // 下载并合成瓦片
                downloadAndComposeTiles(image.tiles, scale)
            }
            .onFailure { error ->
                _uiState.value = SatelliteUiState.Error(
                    error.message ?: "加载卫星图像失败"
                )
            }
    }

    /**
     * 下载并合成瓦片
     */
    private suspend fun downloadAndComposeTiles(tiles: List<SatelliteTile>, scale: Int) {
        val downloadedTiles = mutableListOf<Bitmap>()

        tiles.forEachIndexed { index, tile ->
            satelliteRepository.downloadTile(tile.url)
                .onSuccess { bitmap ->
                    downloadedTiles.add(bitmap)
                    
                    // 更新瓦片加载状态
                    _tileLoadStates.update { states ->
                        states.toMutableMap().apply {
                            this["${tile.x}_${tile.y}"] = TileLoadState(
                                x = tile.x,
                                y = tile.y,
                                isLoading = false,
                                isError = false
                            )
                        }
                    }
                }
                .onFailure {
                    // 更新错误状态
                    _tileLoadStates.update { states ->
                        states.toMutableMap().apply {
                            this["${tile.x}_${tile.y}"] = TileLoadState(
                                x = tile.x,
                                y = tile.y,
                                isLoading = false,
                                isError = true
                            )
                        }
                    }
                }
        }

        // 如果所有瓦片都下载成功，合成图像
        if (downloadedTiles.size == tiles.size) {
            satelliteRepository.composeTiles(downloadedTiles, scale)
                .onSuccess { composedBitmap ->
                    _composedBitmap.value = composedBitmap
                    updateSuccessState()
                }
                .onFailure { error ->
                    _uiState.value = SatelliteUiState.Error(
                        error.message ?: "合成图像失败"
                    )
                }
        } else {
            // 即使部分瓦片失败也更新状态
            updateSuccessState()
        }
    }

    /**
     * 更新成功状态
     */
    private fun updateSuccessState() {
        val image = _currentImage.value ?: return
        val timeline = _timelinePoints.value

        _uiState.value = SatelliteUiState.Success(
            currentImage = image,
            timelinePoints = timeline,
            currentBand = _currentBand.value,
            isAnimating = _isPlaying.value,
            animationSpeed = _animationSpeed.value
        )
    }

    /**
     * 选择时间点
     */
    fun selectTime(index: Int) {
        val timeline = _timelinePoints.value
        if (index !in timeline.indices) return

        _currentTimeIndex.value = index
        
        // 更新时间轴选中状态
        _timelinePoints.value = timeline.mapIndexed { i, point ->
            point.copy(isSelected = i == index)
        }

        viewModelScope.launch {
            loadSatelliteImage(
                timestamp = timeline[index].timestamp,
                band = _currentBand.value,
                scale = _scale.value
            )
        }
    }

    /**
     * 选择波段
     */
    fun selectBand(band: SatelliteBand) {
        if (_currentBand.value == band) return

        _currentBand.value = band

        val timeline = _timelinePoints.value
        val currentIndex = _currentTimeIndex.value

        if (timeline.isNotEmpty() && currentIndex in timeline.indices) {
            viewModelScope.launch {
                loadSatelliteImage(
                    timestamp = timeline[currentIndex].timestamp,
                    band = band,
                    scale = _scale.value
                )
            }
        }
    }

    /**
     * 切换播放/暂停
     */
    fun togglePlayPause() {
        if (_isPlaying.value) {
            stopAnimation()
        } else {
            startAnimation()
        }
    }

    /**
     * 开始动画播放
     */
    fun startAnimation() {
        if (_isPlaying.value) return

        _isPlaying.value = true
        animationJob = viewModelScope.launch {
            val timeline = _timelinePoints.value
            var index = _currentTimeIndex.value

            while (_isPlaying.value) {
                // 移动到下一帧
                index = (index + 1) % timeline.size
                
                // 如果到达最新帧，从头开始
                if (index == 0) {
                    index = 0
                }

                selectTime(index)
                delay(_animationSpeed.value.delayMs)
            }
        }
    }

    /**
     * 停止动画播放
     */
    fun stopAnimation() {
        _isPlaying.value = false
        animationJob?.cancel()
        animationJob = null
    }

    /**
     * 设置动画速度
     */
    fun setAnimationSpeed(speed: AnimationSpeed) {
        _animationSpeed.value = speed
        updateSuccessState()
    }

    /**
     * 上一帧
     */
    fun previousFrame() {
        if (_isPlaying.value) return

        val currentIndex = _currentTimeIndex.value
        val timeline = _timelinePoints.value

        if (currentIndex > 0) {
            selectTime(currentIndex - 1)
        }
    }

    /**
     * 下一帧
     */
    fun nextFrame() {
        if (_isPlaying.value) return

        val currentIndex = _currentTimeIndex.value
        val timeline = _timelinePoints.value

        if (currentIndex < timeline.size - 1) {
            selectTime(currentIndex + 1)
        }
    }

    /**
     * 跳转到最新
     */
    fun jumpToLatest() {
        val timeline = _timelinePoints.value
        if (timeline.isNotEmpty()) {
            selectTime(timeline.size - 1)
        }
    }

    /**
     * 刷新数据
     */
    fun refresh() {
        stopAnimation()
        loadInitialData()
    }

    /**
     * 重试加载失败的瓦片
     */
    fun retryTile(x: Int, y: Int) {
        val image = _currentImage.value ?: return
        val tile = image.tiles.find { it.x == x && it.y == y } ?: return

        viewModelScope.launch {
            // 更新为加载中状态
            _tileLoadStates.update { states ->
                states.toMutableMap().apply {
                    this["${x}_${y}"] = TileLoadState(x, y, isLoading = true, isError = false)
                }
            }

            satelliteRepository.downloadTile(tile.url)
                .onSuccess { bitmap ->
                    _tileLoadStates.update { states ->
                        states.toMutableMap().apply {
                            this["${x}_${y}"] = TileLoadState(x, y, isLoading = false, isError = false)
                        }
                    }
                }
                .onFailure {
                    _tileLoadStates.update { states ->
                        states.toMutableMap().apply {
                            this["${x}_${y}"] = TileLoadState(
                                x, y, isLoading = false, isError = true,
                                retryCount = (states["${x}_${y}"]?.retryCount ?: 0) + 1
                            )
                        }
                    }
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopAnimation()
    }
}
