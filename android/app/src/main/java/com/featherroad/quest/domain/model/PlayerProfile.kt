package com.featherroad.quest.domain.model

data class PlayerProfile(
    val balance: Int,
    val bestRun: Int,
    val missionsDone: Int,
    val vipPoints: Int,
    val selectedMissionId: String,
    val selectedCarId: String,
    val selectedOutfitId: String,
    val paintIndex: Int,
    val soundOn: Boolean,
    val musicOn: Boolean,
    val vibrationOn: Boolean,
    val quality: Int,
    val upgrades: Map<String, Int>,
    val ownedOutfits: Set<String>
)
