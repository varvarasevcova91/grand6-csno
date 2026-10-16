package com.featherroad.quest.presentation.gameover

data class ResultUiState(
    val success: Boolean,
    val missionName: String,
    val cash: Int,
    val diamonds: Int,
    val multiplier: Int,
    val bonusPayout: Int,
    val total: Int,
    val stars: Int,
    val bestRun: Int,
    val vipPoints: Int
) {
    companion object {
        fun empty(): ResultUiState = ResultUiState(
            success = false,
            missionName = "",
            cash = 0,
            diamonds = 0,
            multiplier = 1,
            bonusPayout = 0,
            total = 0,
            stars = 0,
            bestRun = 0,
            vipPoints = 0
        )
    }
}
