package com.featherroad.quest.domain.model

data class RunResult(
    val missionId: String,
    val missionName: String,
    val success: Boolean,
    val cash: Int,
    val diamonds: Int,
    val multiplier: Int,
    val strikes: Int,
    val distance: Int,
    val stars: Int,
    val bonusPayout: Int,
    val total: Int
)
