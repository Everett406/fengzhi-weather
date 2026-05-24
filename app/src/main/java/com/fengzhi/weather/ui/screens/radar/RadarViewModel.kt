package com.fengzhi.weather.ui.screens.radar

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fengzhi.weather.data.model.RadarMapConfig
import com.fengzhi.weather.data.model.RadarState
import com.fengzhi.weather.data.repository.RadarRepository
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
 * 雷达图 ViewModel
 * 管理雷达数据和动画播放
 */
@HiltViewModel
class RadarViewModel @Inject constructor(
    private val radarRepository: RadarRepository
) : ViewModel() {
    
    companion object {
        private const val TAG = "RadarViewModel"
    }
    
    private val _radarState = MutableStateFlow<RadarState>(RadarState.Loading)
    val radarState: StateFlow<RadarState> = _radarState.asStateFlow()
    
    private var animationJob: Job? = null
    
    init {
        loadRadarData()
    }
    
    /**
     * 加载雷达数据
     */
    fun loadRadarData() {
        viewModelScope.launch {
            _radarState.value = RadarState.Loading
            val result = radarRepository.getRadarData()
            _radarState.value = result
            
            if (result is RadarState.Success) {
                Log.d(TAG, "雷达数据加载成功，共 ${result.totalFrames} 帧")
            }
        }
    }
    
    /**
     * 切换播放/暂停
     */
    fun togglePlayPause() {
        val currentState = _radarState.value
        if (currentState is RadarState.Success) {
            if (currentState.isPlaying) {
                stopAnimation()
            } else {
                startAnimation()
            }
        }
    }
    
    /**
     * 开始动画播放
     */
    fun startAnimation() {
        val currentState = _radarState.value as? RadarState.Success ?: return
        
        // 更新播放状态
        _radarState.update { state ->
            (state as RadarState.Success).copy(isPlaying = true)
        }
        
        // 取消之前的动画任务
        animationJob?.cancel()
        
        // 启动新的动画任务
        animationJob = viewModelScope.launch {
            while (true) {
                delay(RadarMapConfig.ANIMATION_INTERVAL)
                
                _radarState.update { state ->
                    val successState = state as? RadarState.Success ?: return@update state
                    val nextIndex = (successState.currentFrameIndex + 1) % successState.totalFrames
                    successState.copy(currentFrameIndex = nextIndex)
                }
            }
        }
        
        Log.d(TAG, "开始雷达动画播放")
    }
    
    /**
     * 停止动画播放
     */
    fun stopAnimation() {
        animationJob?.cancel()
        animationJob = null
        
        _radarState.update { state ->
            (state as? RadarState.Success)?.copy(isPlaying = false) ?: state
        }
        
        Log.d(TAG, "停止雷达动画播放")
    }
    
    /**
     * 跳转到指定帧
     */
    fun seekToFrame(frameIndex: Int) {
        _radarState.update { state ->
            val successState = state as? RadarState.Success ?: return@update state
            val clampedIndex = frameIndex.coerceIn(0, successState.totalFrames - 1)
            successState.copy(currentFrameIndex = clampedIndex)
        }
    }
    
    /**
     * 跳转到进度位置
     */
    fun seekToProgress(progress: Float) {
        val currentState = _radarState.value as? RadarState.Success ?: return
        val frameIndex = (progress * (currentState.totalFrames - 1)).toInt()
        seekToFrame(frameIndex)
    }
    
    /**
     * 跳转到最新帧
     */
    fun seekToLatest() {
        val currentState = _radarState.value as? RadarState.Success ?: return
        val latestPastIndex = currentState.allFrames.indexOfLast { !it.isNowcast() }
        if (latestPastIndex >= 0) {
            seekToFrame(latestPastIndex)
        }
    }
    
    /**
     * 下一帧
     */
    fun nextFrame() {
        val currentState = _radarState.value as? RadarState.Success ?: return
        val nextIndex = (currentState.currentFrameIndex + 1) % currentState.totalFrames
        seekToFrame(nextIndex)
    }
    
    /**
     * 上一帧
     */
    fun previousFrame() {
        val currentState = _radarState.value as? RadarState.Success ?: return
        val prevIndex = if (currentState.currentFrameIndex > 0) {
            currentState.currentFrameIndex - 1
        } else {
            currentState.totalFrames - 1
        }
        seekToFrame(prevIndex)
    }
    
    /**
     * 刷新数据
     */
    fun refresh() {
        stopAnimation()
        loadRadarData()
    }
    
    override fun onCleared() {
        super.onCleared()
        animationJob?.cancel()
    }
}
