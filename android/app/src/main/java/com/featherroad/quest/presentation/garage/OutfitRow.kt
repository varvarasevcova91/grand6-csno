package com.featherroad.quest.presentation.garage

import com.featherroad.quest.domain.model.OutfitModel

data class OutfitRow(
    val outfit: OutfitModel,
    val owned: Boolean,
    val selected: Boolean
)
