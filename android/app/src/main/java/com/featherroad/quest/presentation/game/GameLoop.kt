package com.featherroad.quest.presentation.game

import com.featherroad.quest.core.config.GameConfig
import com.featherroad.quest.domain.model.EntityKind
import com.featherroad.quest.domain.model.Mission
import com.featherroad.quest.domain.model.RoadEntity
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class GameLoop(val mission: Mission) {

    var speed = GameConfig.SPEED_BASE
        private set

    var distance = 0f
        private set

    var cash = 0
        private set

    var diamonds = 0
        private set

    var multiplier = 1
        private set

    var strikes = 0
        private set

    var lane = 1
        private set

    var scrollPhase = 0f
        private set

    var crashTicket = 0L
        private set

    private val activeEntities = mutableListOf<RoadEntity>()

    private var nextId = 1L
    private var spawnAccumulator = 0L
    private var multiplierUntil = 0L
    private var boostUntil = 0L
    private var boostReadyAt = 0L
    private var elapsed = 0L

    val entities: List<RoadEntity>
        get() = activeEntities.toList()

    fun accelerate() {
        speed = min(GameConfig.SPEED_MAX, speed + GameConfig.SPEED_TAP_GAIN)
    }

    fun shiftLane(direction: Int) {
        lane = (lane + direction).coerceIn(0, GameConfig.LANE_COUNT - 1)
    }

    fun requestBoost(): Boolean {
        if (elapsed < boostReadyAt) return false
        boostUntil = elapsed + GameConfig.BOOST_DURATION_MS
        boostReadyAt = elapsed + GameConfig.BOOST_COOLDOWN_MS
        return true
    }

    fun boosting(): Boolean = elapsed < boostUntil

    fun boostReady(): Boolean = elapsed >= boostReadyAt

    fun speedPercent(): Int {
        val span = GameConfig.SPEED_MAX - GameConfig.SPEED_BASE
        val value = (speed - GameConfig.SPEED_BASE) / span
        return (value * 100f).toInt().coerceIn(0, 100)
    }

    fun idleTick() {
        elapsed += GameConfig.TICK_MS
        scrollPhase += GameConfig.SPEED_BASE * GameConfig.TICK_SECONDS * IDLE_SCROLL
    }

    fun tick() {
        elapsed += GameConfig.TICK_MS
        updateSpeed()
        distance += speed * GameConfig.TICK_SECONDS
        scrollPhase += speed * GameConfig.TICK_SECONDS * SCROLL_VISUAL
        if (elapsed > multiplierUntil) multiplier = 1
        advanceEntities()
        resolveCollisions()
        spawn()
    }

    fun finished(): Boolean = distance >= mission.targetDistance || strikes >= GameConfig.MAX_STRIKES

    fun succeeded(): Boolean = distance >= mission.targetDistance

    private fun updateSpeed() {
        speed = if (boosting()) {
            max(speed, GameConfig.SPEED_BOOST)
        } else {
            max(GameConfig.SPEED_BASE, speed - GameConfig.SPEED_DECAY)
        }
    }

    private fun advanceEntities() {
        val delta = speed * GameConfig.TICK_SECONDS * GameConfig.SCROLL_FACTOR
        for (index in activeEntities.indices) {
            val entity = activeEntities[index]
            activeEntities[index] = entity.copy(y = entity.y + delta)
        }
        activeEntities.removeAll { it.y > GameConfig.DESPAWN_Y }
    }

    private fun resolveCollisions() {
        val hit = activeEntities.filter { entity ->
            entity.lane == lane &&
                entity.y >= GameConfig.PLAYER_BAND_TOP &&
                entity.y <= GameConfig.PLAYER_BAND_BOTTOM
        }
        if (hit.isEmpty()) return
        hit.forEach { entity ->
            when (entity.kind) {
                EntityKind.TRAFFIC, EntityKind.POLICE -> {
                    if (!boosting()) {
                        strikes += 1
                        crashTicket = elapsed
                        speed = GameConfig.SPEED_BASE
                    }
                }

                EntityKind.CASH -> cash += GameConfig.CASH_PICKUP * multiplier
                EntityKind.DIAMOND -> diamonds += 1
                EntityKind.MULTIPLIER -> {
                    multiplier = nextMultiplier()
                    multiplierUntil = elapsed + GameConfig.MULTIPLIER_WINDOW_MS
                }
            }
            activeEntities.remove(entity)
        }
    }

    private fun nextMultiplier(): Int {
        val steps = GameConfig.MULTIPLIER_STEPS
        val index = steps.indexOf(multiplier)
        return steps[min(index + 1, steps.size - 1).coerceAtLeast(0)]
    }

    private fun spawn() {
        spawnAccumulator += GameConfig.TICK_MS
        val interval = max(
            GameConfig.SPAWN_MIN_MS,
            GameConfig.SPAWN_BASE_MS - speed.toLong() * GameConfig.SPAWN_SPEED_FACTOR
        )
        if (spawnAccumulator < interval) return
        spawnAccumulator = 0L
        val roll = Random.nextInt(100)
        val kind = when {
            roll < 44 -> EntityKind.TRAFFIC
            roll < 60 -> EntityKind.POLICE
            roll < 84 -> EntityKind.CASH
            roll < 94 -> EntityKind.DIAMOND
            else -> EntityKind.MULTIPLIER
        }
        val targetLane = Random.nextInt(GameConfig.LANE_COUNT)
        if (activeEntities.any { it.lane == targetLane && it.y < SPAWN_GUARD }) return
        activeEntities.add(
            RoadEntity(
                id = nextId++,
                kind = kind,
                lane = targetLane,
                y = SPAWN_Y
            )
        )
    }

    companion object {
        private const val SPAWN_Y = -0.18f
        private const val SPAWN_GUARD = 0.12f
        private const val SCROLL_VISUAL = 2.4f
        private const val IDLE_SCROLL = 0.9f
    }
}
