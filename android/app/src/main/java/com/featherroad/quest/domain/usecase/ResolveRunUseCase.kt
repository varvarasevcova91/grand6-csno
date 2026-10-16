package com.featherroad.quest.domain.usecase

import com.featherroad.quest.core.config.GameConfig
import com.featherroad.quest.domain.model.Mission
import com.featherroad.quest.domain.model.RunResult
import com.featherroad.quest.domain.repository.ProfileRepository

class ResolveRunUseCase(
    private val profileRepository: ProfileRepository
) {

    operator fun invoke(
        mission: Mission,
        cash: Int,
        diamonds: Int,
        multiplier: Int,
        strikes: Int,
        distance: Int,
        success: Boolean
    ): RunResult {
        val banked = if (success) cash else (cash * GameConfig.BUSTED_CASH_SHARE).toInt()
        val stars = when {
            !success -> 0
            strikes == 0 -> 3
            strikes == 1 -> 2
            else -> 1
        }
        return RunResult(
            missionId = mission.id,
            missionName = mission.name,
            success = success,
            cash = banked,
            diamonds = diamonds,
            multiplier = multiplier,
            strikes = strikes,
            distance = distance,
            stars = stars,
            bonusPayout = 0,
            total = banked
        )
    }

    fun commit(result: RunResult) {
        profileRepository.registerRun(result.total, result.distance, result.success)
    }
}
