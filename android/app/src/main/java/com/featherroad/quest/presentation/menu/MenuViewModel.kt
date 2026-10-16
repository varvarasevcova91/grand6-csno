package com.featherroad.quest.presentation.menu

import androidx.lifecycle.ViewModel
import com.featherroad.quest.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MenuViewModel(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val mutableState = MutableStateFlow(MenuUiState.empty())
    val state: StateFlow<MenuUiState> = mutableState.asStateFlow()

    fun refresh() {
        val profile = profileRepository.profile()
        mutableState.value = MenuUiState(
            balance = profile.balance,
            bestRun = profile.bestRun,
            missionsDone = profile.missionsDone,
            vipRank = 1 + profile.vipPoints / RANK_STEP
        )
    }

    companion object {
        private const val RANK_STEP = 60
    }
}
