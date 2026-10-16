package com.featherroad.quest.domain.usecase

import com.featherroad.quest.domain.model.CarModel
import com.featherroad.quest.domain.repository.ProfileRepository

class UpgradeCarUseCase(
    private val profileRepository: ProfileRepository
) {

    operator fun invoke(car: CarModel): Boolean {
        val profile = profileRepository.profile()
        val level = profile.upgrades[car.id] ?: 0
        if (level >= MAX_LEVEL) return false
        if (profile.balance < car.upgradeCost) return false
        val upgrades = profile.upgrades.toMutableMap()
        upgrades[car.id] = level + 1
        profileRepository.save(
            profile.copy(
                balance = profile.balance - car.upgradeCost,
                upgrades = upgrades
            )
        )
        return true
    }

    companion object {
        const val MAX_LEVEL = 3
    }
}
