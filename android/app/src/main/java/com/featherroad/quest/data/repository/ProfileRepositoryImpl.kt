package com.featherroad.quest.data.repository

import com.featherroad.quest.core.config.GameConfig
import com.featherroad.quest.data.local.PreferencesStorage
import com.featherroad.quest.data.sample.SampleData
import com.featherroad.quest.domain.model.PlayerProfile
import com.featherroad.quest.domain.repository.ProfileRepository
import kotlin.math.max

class ProfileRepositoryImpl(
    private val storage: PreferencesStorage
) : ProfileRepository {

    override fun profile(): PlayerProfile {
        return PlayerProfile(
            balance = storage.readInt(PreferencesStorage.KEY_BALANCE, GameConfig.STARTING_BALANCE),
            bestRun = storage.readInt(PreferencesStorage.KEY_BEST_RUN, 4820),
            missionsDone = storage.readInt(PreferencesStorage.KEY_MISSIONS_DONE, 17),
            vipPoints = storage.readInt(PreferencesStorage.KEY_VIP_POINTS, 128),
            selectedMissionId = storage.readString(
                PreferencesStorage.KEY_MISSION,
                SampleData.DEFAULT_MISSION_ID
            ),
            selectedCarId = storage.readString(
                PreferencesStorage.KEY_CAR,
                SampleData.DEFAULT_CAR_ID
            ),
            selectedOutfitId = storage.readString(
                PreferencesStorage.KEY_OUTFIT,
                SampleData.DEFAULT_OUTFIT_ID
            ),
            paintIndex = storage.readInt(PreferencesStorage.KEY_PAINT, 0),
            soundOn = storage.readBool(PreferencesStorage.KEY_SOUND, true),
            musicOn = storage.readBool(PreferencesStorage.KEY_MUSIC, true),
            vibrationOn = storage.readBool(PreferencesStorage.KEY_VIBRATION, true),
            quality = storage.readInt(PreferencesStorage.KEY_QUALITY, 1),
            upgrades = decodeUpgrades(storage.readString(PreferencesStorage.KEY_UPGRADES, "")),
            ownedOutfits = decodeOwned(
                storage.readString(
                    PreferencesStorage.KEY_OWNED_OUTFITS,
                    SampleData.DEFAULT_OUTFIT_ID
                )
            )
        )
    }

    override fun save(profile: PlayerProfile) {
        storage.writeInt(PreferencesStorage.KEY_BALANCE, max(0, profile.balance))
        storage.writeInt(PreferencesStorage.KEY_BEST_RUN, profile.bestRun)
        storage.writeInt(PreferencesStorage.KEY_MISSIONS_DONE, profile.missionsDone)
        storage.writeInt(PreferencesStorage.KEY_VIP_POINTS, profile.vipPoints)
        storage.writeString(PreferencesStorage.KEY_MISSION, profile.selectedMissionId)
        storage.writeString(PreferencesStorage.KEY_CAR, profile.selectedCarId)
        storage.writeString(PreferencesStorage.KEY_OUTFIT, profile.selectedOutfitId)
        storage.writeInt(PreferencesStorage.KEY_PAINT, profile.paintIndex)
        storage.writeBool(PreferencesStorage.KEY_SOUND, profile.soundOn)
        storage.writeBool(PreferencesStorage.KEY_MUSIC, profile.musicOn)
        storage.writeBool(PreferencesStorage.KEY_VIBRATION, profile.vibrationOn)
        storage.writeInt(PreferencesStorage.KEY_QUALITY, profile.quality)
        storage.writeString(PreferencesStorage.KEY_UPGRADES, encodeUpgrades(profile.upgrades))
        storage.writeString(
            PreferencesStorage.KEY_OWNED_OUTFITS,
            profile.ownedOutfits.joinToString(SEPARATOR)
        )
    }

    override fun addCash(amount: Int): PlayerProfile {
        val current = profile()
        val updated = current.copy(balance = max(0, current.balance + amount))
        save(updated)
        return updated
    }

    override fun selectMission(missionId: String) {
        storage.writeString(PreferencesStorage.KEY_MISSION, missionId)
    }

    override fun registerRun(cash: Int, distance: Int, success: Boolean): PlayerProfile {
        val current = profile()
        val updated = current.copy(
            balance = max(0, current.balance + cash),
            bestRun = max(current.bestRun, distance),
            missionsDone = if (success) current.missionsDone + 1 else current.missionsDone,
            vipPoints = current.vipPoints + cash / GameConfig.VIP_POINTS_DIVISOR
        )
        save(updated)
        return updated
    }

    override fun reset(): PlayerProfile {
        storage.clear()
        val fresh = profile().copy(
            balance = GameConfig.FRESH_PROFILE_BALANCE,
            bestRun = 0,
            missionsDone = 0,
            vipPoints = 0,
            upgrades = emptyMap(),
            ownedOutfits = setOf(SampleData.DEFAULT_OUTFIT_ID)
        )
        save(fresh)
        return fresh
    }

    private fun encodeUpgrades(upgrades: Map<String, Int>): String {
        return upgrades.entries.joinToString(SEPARATOR) { entry -> entry.key + PAIR + entry.value }
    }

    private fun decodeUpgrades(raw: String): Map<String, Int> {
        if (raw.isEmpty()) return emptyMap()
        val result = mutableMapOf<String, Int>()
        raw.split(SEPARATOR).forEach { chunk ->
            val parts = chunk.split(PAIR)
            if (parts.size == 2) {
                val level = parts[1].toIntOrNull()
                if (level != null) result[parts[0]] = level
            }
        }
        return result
    }

    private fun decodeOwned(raw: String): Set<String> {
        if (raw.isEmpty()) return setOf(SampleData.DEFAULT_OUTFIT_ID)
        return raw.split(SEPARATOR).filter { it.isNotEmpty() }.toSet()
    }

    companion object {
        private const val SEPARATOR = ";"
        private const val PAIR = ":"
    }
}
