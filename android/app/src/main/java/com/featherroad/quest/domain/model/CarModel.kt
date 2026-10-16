package com.featherroad.quest.domain.model

data class CarModel(
    val id: String,
    val name: String,
    val spriteKey: String,
    val speed: Int,
    val grip: Int,
    val nitro: Int,
    val upgradeCost: Int
)
