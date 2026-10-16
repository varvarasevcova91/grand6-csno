package com.featherroad.quest.core.ui

import com.featherroad.quest.R
import com.featherroad.quest.domain.model.BonusSymbol
import com.featherroad.quest.domain.model.EntityKind

object SpriteCatalog {

    fun car(spriteKey: String): Int = when (spriteKey) {
        "traffic" -> R.drawable.sprite_car_traffic
        else -> R.drawable.sprite_car_hero
    }

    fun entity(kind: EntityKind): Int = when (kind) {
        EntityKind.TRAFFIC -> R.drawable.sprite_car_traffic
        EntityKind.POLICE -> R.drawable.sprite_police_car
        EntityKind.CASH -> R.drawable.sprite_cash_bundle
        EntityKind.DIAMOND -> R.drawable.sprite_diamond
        EntityKind.MULTIPLIER -> R.drawable.sprite_multiplier
    }

    fun entityWidth(kind: EntityKind): Int = when (kind) {
        EntityKind.TRAFFIC -> R.dimen.traffic_car_width
        EntityKind.POLICE -> R.dimen.police_car_width
        EntityKind.CASH -> R.dimen.pickup_cash
        EntityKind.DIAMOND -> R.dimen.pickup_diamond
        EntityKind.MULTIPLIER -> R.dimen.pickup_multiplier
    }

    fun entityHeight(kind: EntityKind): Int = when (kind) {
        EntityKind.TRAFFIC -> R.dimen.traffic_car_height
        EntityKind.POLICE -> R.dimen.police_car_height
        EntityKind.CASH -> R.dimen.pickup_cash
        EntityKind.DIAMOND -> R.dimen.pickup_diamond
        EntityKind.MULTIPLIER -> R.dimen.pickup_multiplier
    }

    fun bonus(symbol: BonusSymbol): Int = when (symbol) {
        BonusSymbol.SEVEN -> R.drawable.sprite_seven_gold
        BonusSymbol.DIAMOND -> R.drawable.sprite_diamond
        BonusSymbol.STAR -> R.drawable.sprite_star_gold
        BonusSymbol.CASH -> R.drawable.sprite_cash_bundle
        BonusSymbol.TOKEN -> R.drawable.sprite_multiplier
    }

    fun bonusReelOrder(): List<Int> = listOf(
        R.drawable.sprite_seven_gold,
        R.drawable.sprite_diamond,
        R.drawable.sprite_star_gold,
        R.drawable.sprite_cash_bundle,
        R.drawable.sprite_multiplier
    )
}
