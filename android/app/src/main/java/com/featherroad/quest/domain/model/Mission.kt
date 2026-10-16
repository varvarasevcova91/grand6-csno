package com.featherroad.quest.domain.model

data class Mission(
    val id: String,
    val name: String,
    val reward: Int,
    val risk: String,
    val difficulty: Int,
    val targetDistance: Int,
    val unlockCost: Int
)
