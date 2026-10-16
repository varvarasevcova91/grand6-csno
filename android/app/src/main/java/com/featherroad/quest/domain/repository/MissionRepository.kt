package com.featherroad.quest.domain.repository

import com.featherroad.quest.domain.model.Mission

interface MissionRepository {

    fun missions(): List<Mission>

    fun missionById(id: String): Mission

    fun firstMission(): Mission
}
