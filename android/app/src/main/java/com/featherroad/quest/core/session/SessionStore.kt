package com.featherroad.quest.core.session

import com.featherroad.quest.domain.model.BonusSpin
import com.featherroad.quest.domain.model.RunResult

object SessionStore {

    var lastRun: RunResult? = null
        private set

    var lastBonus: BonusSpin? = null
        private set

    fun storeRun(result: RunResult) {
        lastRun = result
    }

    fun storeBonus(spin: BonusSpin) {
        lastBonus = spin
    }

    fun applyBonus(payout: Int) {
        val current = lastRun ?: return
        lastRun = current.copy(bonusPayout = payout, total = current.cash + payout)
    }

    fun clearBonus() {
        lastBonus = null
    }
}
