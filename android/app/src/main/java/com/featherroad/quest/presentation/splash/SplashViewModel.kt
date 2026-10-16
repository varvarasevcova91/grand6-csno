package com.featherroad.quest.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.featherroad.quest.core.config.GameConfig
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SplashViewModel : ViewModel() {

    private val mutableState = MutableStateFlow(SplashUiState.initial())
    val state: StateFlow<SplashUiState> = mutableState.asStateFlow()

    private var timerJob: Job? = null
    private var navigationConsumed = false

    fun startTimer() {
        if (timerJob != null) return
        timerJob = viewModelScope.launch {
            delay(GameConfig.LOADER_DURATION_MS)
            if (!navigationConsumed) {
                mutableState.value = mutableState.value.copy(readyToNavigate = true)
            }
        }
    }

    fun consumeNavigation() {
        navigationConsumed = true
        mutableState.value = mutableState.value.copy(readyToNavigate = false)
    }

    override fun onCleared() {
        timerJob?.cancel()
        timerJob = null
        super.onCleared()
    }
}
