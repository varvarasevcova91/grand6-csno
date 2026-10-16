package com.featherroad.quest.domain.usecase

import com.featherroad.quest.domain.model.Mission
import com.featherroad.quest.domain.repository.ProfileRepository

class UnlockDistrictUseCase(
    private val profileRepository: ProfileRepository
) {

    operator fun invoke(mission: Mission): Boolean {
        return profileRepository.profile().vipPoints >= mission.unlockCost
    }
}
