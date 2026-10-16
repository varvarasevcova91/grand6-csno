package com.featherroad.quest.domain.repository

import com.featherroad.quest.domain.model.PlayerProfile

interface ProfileRepository {

    fun profile(): PlayerProfile

    fun save(profile: PlayerProfile)

    fun addCash(amount: Int): PlayerProfile

    fun selectMission(missionId: String)

    fun registerRun(cash: Int, distance: Int, success: Boolean): PlayerProfile

    fun reset(): PlayerProfile
}
