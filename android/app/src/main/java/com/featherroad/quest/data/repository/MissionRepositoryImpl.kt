package com.featherroad.quest.data.repository

import com.featherroad.quest.data.sample.SampleData
import com.featherroad.quest.domain.model.Mission
import com.featherroad.quest.domain.repository.MissionRepository

class MissionRepositoryImpl : MissionRepository {

    override fun missions(): List<Mission> = SampleData.missions

    override fun missionById(id: String): Mission {
        return SampleData.missions.firstOrNull { it.id == id } ?: SampleData.missions[0]
    }

    override fun firstMission(): Mission = SampleData.missions[0]
}
