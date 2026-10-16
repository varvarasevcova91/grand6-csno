package com.featherroad.quest.data.local

import android.content.Context
import android.content.SharedPreferences

class PreferencesStorage(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(STORE_NAME, Context.MODE_PRIVATE)

    fun readInt(key: String, fallback: Int): Int = prefs.getInt(key, fallback)

    fun writeInt(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }

    fun readBool(key: String, fallback: Boolean): Boolean = prefs.getBoolean(key, fallback)

    fun writeBool(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    fun readString(key: String, fallback: String): String = prefs.getString(key, fallback) ?: fallback

    fun writeString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val STORE_NAME = "grand6csno_profile"

        const val KEY_BALANCE = "balance"
        const val KEY_BEST_RUN = "best_run"
        const val KEY_MISSIONS_DONE = "missions_done"
        const val KEY_VIP_POINTS = "vip_points"
        const val KEY_MISSION = "selected_mission"
        const val KEY_CAR = "selected_car"
        const val KEY_OUTFIT = "selected_outfit"
        const val KEY_PAINT = "paint_index"
        const val KEY_SOUND = "sound_on"
        const val KEY_MUSIC = "music_on"
        const val KEY_VIBRATION = "vibration_on"
        const val KEY_QUALITY = "quality"
        const val KEY_UPGRADES = "upgrades"
        const val KEY_OWNED_OUTFITS = "owned_outfits"
    }
}
