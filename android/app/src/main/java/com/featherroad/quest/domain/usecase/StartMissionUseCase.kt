package com.featherroad.quest.domain.usecase

import com.featherroad.quest.domain.model.Mission
import com.featherroad.quest.domain.repository.MissionRepository
import com.featherroad.quest.domain.repository.ProfileRepository

class StartMissionUseCase(
    private val profileRepository: ProfileRepository,
    private val missionRepository: MissionRepository,
    private val unlockDistrictUseCase: UnlockDistrictUseCase
) {

    operator fun invoke(missionId: String?): Mission {
        val profile = profileRepository.profile()
        val requested = missionId ?: profile.selectedMissionId
        val mission = missionRepository.missionById(requested)
        val playable = if (unlockDistrictUseCase(mission)) mission else missionRepository.firstMission()
        profileRepository.selectMission(playable.id)
        return playable
    }
}
