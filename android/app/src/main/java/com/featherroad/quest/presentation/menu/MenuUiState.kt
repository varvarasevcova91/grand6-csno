package com.featherroad.quest.presentation.menu

data class MenuUiState(
    val balance: Int,
    val bestRun: Int,
    val missionsDone: Int,
    val vipRank: Int
) {
    companion object {
        fun empty(): MenuUiState = MenuUiState(
            balance = 0,
            bestRun = 0,
            missionsDone = 0,
            vipRank = 1
        )
    }
}
