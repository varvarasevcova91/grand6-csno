package com.featherroad.quest.presentation.map

import androidx.lifecycle.ViewModel
import com.featherroad.quest.domain.repository.MissionRepository
import com.featherroad.quest.domain.repository.ProfileRepository
import com.featherroad.quest.domain.usecase.StartMissionUseCase
import com.featherroad.quest.domain.usecase.UnlockDistrictUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CityMapViewModel(
    private val profileRepository: ProfileRepository,
    private val missionRepository: MissionRepository,
    private val unlockDistrictUseCase: UnlockDistrictUseCase,
    private val startMissionUseCase: StartMissionUseCase
) : ViewModel() {

    private val mutableRows = MutableStateFlow<List<MissionRow>>(emptyList())
    val rows: StateFlow<List<MissionRow>> = mutableRows.asStateFlow()

    private val mutableBalance = MutableStateFlow(0)
    val balance: StateFlow<Int> = mutableBalance.asStateFlow()

    private var selectedId: String? = null

    fun refresh() {
        val profile = profileRepository.profile()
        mutableBalance.value = profile.balance
        val current = selectedId ?: profile.selectedMissionId
        selectedId = current
        mutableRows.value = missionRepository.missions().map { mission ->
            MissionRow(
                mission = mission,
                unlocked = unlockDistrictUseCase(mission),
                selected = mission.id == current
            )
        }
    }

    fun select(row: MissionRow) {
        if (!row.unlocked) return
        selectedId = row.mission.id
        refresh()
    }

    fun canStart(): Boolean = mutableRows.value.any { it.selected && it.unlocked }

    fun commitSelection() {
        startMissionUseCase(selectedId)
    }
}
