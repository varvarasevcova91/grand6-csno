package com.featherroad.quest.presentation.game

import com.featherroad.quest.domain.model.RoadEntity

data class GameUiState(
    val phase: GamePhase,
    val missionName: String,
    val distance: Int,
    val targetDistance: Int,
    val cash: Int,
    val diamonds: Int,
    val diamondTarget: Int,
    val multiplier: Int,
    val speedPercent: Int,
    val lane: Int,
    val strikes: Int,
    val entities: List<RoadEntity>,
    val scrollPhase: Float,
    val boosting: Boolean,
    val boostReady: Boolean,
    val crashTicket: Long,
    val success: Boolean
) {
    companion object {
        fun initial(): GameUiState = GameUiState(
            phase = GamePhase.READY,
            missionName = "",
            distance = 0,
            targetDistance = 1,
            cash = 0,
            diamonds = 0,
            diamondTarget = 3,
            multiplier = 1,
            speedPercent = 0,
            lane = 1,
            strikes = 0,
            entities = emptyList(),
            scrollPhase = 0f,
            boosting = false,
            boostReady = true,
            crashTicket = 0L,
            success = false
        )
    }
}
