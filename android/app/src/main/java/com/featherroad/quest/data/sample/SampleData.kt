package com.featherroad.quest.data.sample

import com.featherroad.quest.domain.model.CarModel
import com.featherroad.quest.domain.model.Mission
import com.featherroad.quest.domain.model.OutfitModel

object SampleData {

    const val DEFAULT_MISSION_ID = "ocean_drive"
    const val DEFAULT_CAR_ID = "vice_gt"
    const val DEFAULT_OUTFIT_ID = "street_jacket"

    val missions: List<Mission> = listOf(
        Mission(
            id = "ocean_drive",
            name = "OCEAN DRIVE",
            reward = 900,
            risk = "LOW",
            difficulty = 1,
            targetDistance = 700,
            unlockCost = 0
        ),
        Mission(
            id = "downtown_lights",
            name = "DOWNTOWN LIGHTS",
            reward = 1400,
            risk = "MEDIUM",
            difficulty = 2,
            targetDistance = 1000,
            unlockCost = 40
        ),
        Mission(
            id = "neon_harbor",
            name = "NEON HARBOR",
            reward = 2100,
            risk = "HIGH",
            difficulty = 2,
            targetDistance = 1400,
            unlockCost = 110
        ),
        Mission(
            id = "palm_causeway",
            name = "PALM CAUSEWAY",
            reward = 2900,
            risk = "HIGH",
            difficulty = 3,
            targetDistance = 1800,
            unlockCost = 220
        ),
        Mission(
            id = "vice_heights",
            name = "VICE HEIGHTS",
            reward = 3800,
            risk = "EXTREME",
            difficulty = 3,
            targetDistance = 2200,
            unlockCost = 380
        )
    )

    val cars: List<CarModel> = listOf(
        CarModel(
            id = "vice_gt",
            name = "VICE GT",
            spriteKey = "hero",
            speed = 72,
            grip = 58,
            nitro = 64,
            upgradeCost = 1200
        ),
        CarModel(
            id = "neon_spyder",
            name = "NEON SPYDER",
            spriteKey = "traffic",
            speed = 84,
            grip = 50,
            nitro = 70,
            upgradeCost = 1800
        ),
        CarModel(
            id = "harbor_muscle",
            name = "HARBOR MUSCLE",
            spriteKey = "hero",
            speed = 66,
            grip = 78,
            nitro = 52,
            upgradeCost = 2400
        ),
        CarModel(
            id = "palm_cruiser",
            name = "PALM CRUISER",
            spriteKey = "traffic",
            speed = 58,
            grip = 86,
            nitro = 44,
            upgradeCost = 3000
        )
    )

    val outfits: List<OutfitModel> = listOf(
        OutfitModel(id = "street_jacket", name = "STREET JACKET", price = 0),
        OutfitModel(id = "gold_chain_set", name = "GOLD CHAIN SET", price = 1500),
        OutfitModel(id = "neon_shades", name = "NEON SHADES", price = 2200),
        OutfitModel(id = "vip_tuxedo", name = "VIP TUXEDO", price = 4800)
    )
}
