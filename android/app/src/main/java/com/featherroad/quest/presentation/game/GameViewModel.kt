package com.featherroad.quest.presentation.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.featherroad.quest.core.config.GameConfig
import com.featherroad.quest.core.session.SessionStore
import com.featherroad.quest.domain.usecase.ResolveRunUseCase
import com.featherroad.quest.domain.usecase.StartMissionUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class GameViewModel(
    private val startMissionUseCase: StartMissionUseCase,
    private val resolveRunUseCase: ResolveRunUseCase
) : ViewModel() {

    private val mutableState = MutableStateFlow(GameUiState.initial())
    val state: StateFlow<GameUiState> = mutableState.asStateFlow()

    private var loop: GameLoop? = null
    private var tickJob: Job? = null
    private var phase = GamePhase.READY
    private var readyElapsed = 0L
    private var paused = false
    private var resolved = false

    fun start() {
        if (loop != null) return
        restart()
    }

    fun restart() {
        tickJob?.cancel()
        val mission = startMissionUseCase(null)
        val created = GameLoop(mission)
        loop = created
        phase = GamePhase.READY
        readyElapsed = 0L
        paused = false
        resolved = false
        publish()
        tickJob = viewModelScope.launch {
            while (isActive) {
                delay(GameConfig.TICK_MS)
                step()
            }
        }
    }

    fun setPaused(value: Boolean) {
        paused = value
    }

    fun onTap() {
        val current = loop ?: return
        if (phase == GamePhase.READY) {
            phase = GamePhase.RUNNING
            return
        }
        if (phase != GamePhase.RUNNING) return
        current.accelerate()
    }

    fun onSwipe(direction: Int) {
        val current = loop ?: return
        if (phase == GamePhase.READY) phase = GamePhase.RUNNING
        if (phase != GamePhase.RUNNING) return
        current.shiftLane(direction)
    }

    fun onBoost() {
        val current = loop ?: return
        if (phase == GamePhase.READY) phase = GamePhase.RUNNING
        if (phase != GamePhase.RUNNING) return
        current.requestBoost()
    }

    private fun step() {
        val current = loop ?: return
        if (paused || phase == GamePhase.FINISHED) return
        if (phase == GamePhase.READY) {
            current.idleTick()
            readyElapsed += GameConfig.TICK_MS
            if (readyElapsed >= GameConfig.READY_HINT_MS) phase = GamePhase.RUNNING
            publish()
            return
        }
        current.tick()
        if (current.finished()) {
            phase = GamePhase.FINISHED
            resolve(current)
        }
        publish()
    }

    private fun resolve(current: GameLoop) {
        if (resolved) return
        resolved = true
        val result = resolveRunUseCase(
            mission = current.mission,
            cash = current.cash,
            diamonds = current.diamonds,
            multiplier = current.multiplier,
            strikes = current.strikes,
            distance = current.distance.toInt(),
            success = current.succeeded()
        )
        SessionStore.storeRun(result)
        SessionStore.clearBonus()
    }

    private fun publish() {
        val current = loop ?: return
        mutableState.value = GameUiState(
            phase = phase,
            missionName = current.mission.name,
            distance = current.distance.toInt(),
            targetDistance = current.mission.targetDistance,
            cash = current.cash,
            diamonds = current.diamonds,
            diamondTarget = GameConfig.DIAMOND_TARGET,
            multiplier = current.multiplier,
            speedPercent = current.speedPercent(),
            lane = current.lane,
            strikes = current.strikes,
            entities = current.entities,
            scrollPhase = current.scrollPhase,
            boosting = current.boosting(),
            boostReady = current.boostReady(),
            crashTicket = current.crashTicket,
            success = current.succeeded()
        )
    }

    override fun onCleared() {
        tickJob?.cancel()
        tickJob = null
        loop = null
        super.onCleared()
    }
}
