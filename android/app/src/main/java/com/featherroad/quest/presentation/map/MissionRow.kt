package com.featherroad.quest.presentation.map

import com.featherroad.quest.domain.model.Mission

data class MissionRow(
    val mission: Mission,
    val unlocked: Boolean,
    val selected: Boolean
)
