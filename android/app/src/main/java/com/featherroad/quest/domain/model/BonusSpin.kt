package com.featherroad.quest.domain.model

data class BonusSpin(
    val symbols: List<BonusSymbol>,
    val payout: Int,
    val tripleMatch: Boolean
)
