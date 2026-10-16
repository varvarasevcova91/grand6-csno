package com.featherroad.quest.presentation.gameover

import androidx.lifecycle.ViewModel
import com.featherroad.quest.core.session.SessionStore
import com.featherroad.quest.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ResultViewModel(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val mutableState = MutableStateFlow(ResultUiState.empty())
    val state: StateFlow<ResultUiState> = mutableState.asStateFlow()

    private var committed = false

    fun load() {
        val run = SessionStore.lastRun
        if (run == null) {
            mutableState.value = ResultUiState.empty()
            return
        }
        if (!committed) {
            committed = true
            profileRepository.registerRun(run.total, run.distance, run.success)
        }
        val profile = profileRepository.profile()
        mutableState.value = ResultUiState(
            success = run.success,
            missionName = run.missionName,
            cash = run.cash,
            diamonds = run.diamonds,
            multiplier = run.multiplier,
            bonusPayout = run.bonusPayout,
            total = run.total,
            stars = run.stars,
            bestRun = profile.bestRun,
            vipPoints = profile.vipPoints
        )
    }
}
