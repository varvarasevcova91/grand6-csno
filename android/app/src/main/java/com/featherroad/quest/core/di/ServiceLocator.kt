package com.featherroad.quest.core.di

import android.content.Context
import com.featherroad.quest.data.local.PreferencesStorage
import com.featherroad.quest.data.repository.GarageRepositoryImpl
import com.featherroad.quest.data.repository.MissionRepositoryImpl
import com.featherroad.quest.data.repository.ProfileRepositoryImpl
import com.featherroad.quest.domain.repository.GarageRepository
import com.featherroad.quest.domain.repository.MissionRepository
import com.featherroad.quest.domain.repository.ProfileRepository
import com.featherroad.quest.domain.usecase.ResolveRunUseCase
import com.featherroad.quest.domain.usecase.SpinVipBonusUseCase
import com.featherroad.quest.domain.usecase.StartMissionUseCase
import com.featherroad.quest.domain.usecase.UnlockDistrictUseCase
import com.featherroad.quest.domain.usecase.UpgradeCarUseCase

object ServiceLocator {

    private var appContext: Context? = null
    private var profileRepositoryRef: ProfileRepository? = null
    private var missionRepositoryRef: MissionRepository? = null
    private var garageRepositoryRef: GarageRepository? = null

    fun init(context: Context) {
        if (appContext == null) {
            appContext = context.applicationContext
        }
    }

    val profileRepository: ProfileRepository
        get() {
            val existing = profileRepositoryRef
            if (existing != null) return existing
            val created = ProfileRepositoryImpl(PreferencesStorage(requireContext()))
            profileRepositoryRef = created
            return created
        }

    val missionRepository: MissionRepository
        get() {
            val existing = missionRepositoryRef
            if (existing != null) return existing
            val created = MissionRepositoryImpl()
            missionRepositoryRef = created
            return created
        }

    val garageRepository: GarageRepository
        get() {
            val existing = garageRepositoryRef
            if (existing != null) return existing
            val created = GarageRepositoryImpl()
            garageRepositoryRef = created
            return created
        }

    val unlockDistrictUseCase: UnlockDistrictUseCase
        get() = UnlockDistrictUseCase(profileRepository)

    val startMissionUseCase: StartMissionUseCase
        get() = StartMissionUseCase(profileRepository, missionRepository, unlockDistrictUseCase)

    val resolveRunUseCase: ResolveRunUseCase
        get() = ResolveRunUseCase(profileRepository)

    val spinVipBonusUseCase: SpinVipBonusUseCase
        get() = SpinVipBonusUseCase()

    val upgradeCarUseCase: UpgradeCarUseCase
        get() = UpgradeCarUseCase(profileRepository)

    private fun requireContext(): Context {
        return requireNotNull(appContext) { "ServiceLocator.init(context) must run before use" }
    }
}
