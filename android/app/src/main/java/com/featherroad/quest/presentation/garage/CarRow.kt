package com.featherroad.quest.presentation.garage

import com.featherroad.quest.domain.model.CarModel

data class CarRow(
    val car: CarModel,
    val level: Int,
    val selected: Boolean
)
