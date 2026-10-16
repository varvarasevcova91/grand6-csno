package com.featherroad.quest.domain.usecase

import com.featherroad.quest.core.config.GameConfig
import com.featherroad.quest.domain.model.BonusSpin
import com.featherroad.quest.domain.model.BonusSymbol
import kotlin.random.Random

class SpinVipBonusUseCase {

    operator fun invoke(runMultiplier: Int): BonusSpin {
        val pool = BonusSymbol.values()
        val symbols = List(3) { pool[Random.nextInt(pool.size)] }
        val first = symbols[0]
        val triple = symbols.all { it == first }
        val pair = symbols.distinct().size == 2
        val factor = when {
            triple -> first.multiplier
            pair -> 2
            else -> 1
        }
        val payout = GameConfig.BONUS_BET * factor * runMultiplier
        return BonusSpin(symbols = symbols, payout = payout, tripleMatch = triple)
    }
}
