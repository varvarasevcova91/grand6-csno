package com.featherroad.quest.core.config

object GameConfig {

    const val LOADER_DURATION_MS = 8000L

    const val LANE_COUNT = 3
    const val TICK_MS = 16L
    const val TICK_SECONDS = 0.016f

    const val SPEED_BASE = 34f
    const val SPEED_MAX = 118f
    const val SPEED_TAP_GAIN = 12f
    const val SPEED_DECAY = 0.6f
    const val SPEED_BOOST = 92f

    const val SCROLL_FACTOR = 0.015f
    const val LANE_CHANGE_MS = 140L

    const val SPAWN_BASE_MS = 900L
    const val SPAWN_MIN_MS = 280L
    const val SPAWN_SPEED_FACTOR = 4L

    const val PLAYER_BAND_TOP = 0.70f
    const val PLAYER_BAND_BOTTOM = 0.92f
    const val PLAYER_Y = 0.81f
    const val DESPAWN_Y = 1.12f

    const val MAX_STRIKES = 3
    const val CASH_PICKUP = 120
    const val DIAMOND_TARGET = 3
    const val MULTIPLIER_WINDOW_MS = 6000L

    const val BOOST_DURATION_MS = 2500L
    const val BOOST_COOLDOWN_MS = 8000L

    const val CRASH_FREEZE_MS = 600L
    const val FINISH_WIPE_MS = 500L
    const val READY_HINT_MS = 2000L

    const val BONUS_BET = 200
    const val BONUS_REEL_ONE_MS = 1400L
    const val BONUS_REEL_TWO_MS = 1750L
    const val BONUS_REEL_THREE_MS = 2100L

    const val STARTING_BALANCE = 12450
    const val FRESH_PROFILE_BALANCE = 500
    const val BUSTED_CASH_SHARE = 0.5f
    const val VIP_POINTS_DIVISOR = 100

    val MULTIPLIER_STEPS = listOf(2, 3, 5)
    val PAINT_COLORS = listOf("#FF00D6", "#00E5FF", "#8B2CFF", "#FFD700", "#FF6B00", "#FFFFFF")
}
