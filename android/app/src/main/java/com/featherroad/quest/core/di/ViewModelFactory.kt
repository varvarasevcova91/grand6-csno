package com.featherroad.quest.core.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.featherroad.quest.presentation.bonus.VipBonusViewModel
import com.featherroad.quest.presentation.game.GameViewModel
import com.featherroad.quest.presentation.garage.GarageViewModel
import com.featherroad.quest.presentation.gameover.ResultViewModel
import com.featherroad.quest.presentation.map.CityMapViewModel
import com.featherroad.quest.presentation.menu.MenuViewModel
import com.featherroad.quest.presentation.splash.SplashViewModel

class ViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val created: ViewModel = when {
            modelClass.isAssignableFrom(SplashViewModel::class.java) ->
                SplashViewModel()

            modelClass.isAssignableFrom(MenuViewModel::class.java) ->
                MenuViewModel(ServiceLocator.profileRepository)

            modelClass.isAssignableFrom(CityMapViewModel::class.java) ->
                CityMapViewModel(
                    ServiceLocator.profileRepository,
                    ServiceLocator.missionRepository,
                    ServiceLocator.unlockDistrictUseCase,
                    ServiceLocator.startMissionUseCase
                )

            modelClass.isAssignableFrom(GameViewModel::class.java) ->
                GameViewModel(
                    ServiceLocator.startMissionUseCase,
                    ServiceLocator.resolveRunUseCase
                )

            modelClass.isAssignableFrom(VipBonusViewModel::class.java) ->
                VipBonusViewModel(
                    ServiceLocator.spinVipBonusUseCase,
                    ServiceLocator.profileRepository
                )

            modelClass.isAssignableFrom(ResultViewModel::class.java) ->
                ResultViewModel(ServiceLocator.profileRepository)

            modelClass.isAssignableFrom(GarageViewModel::class.java) ->
                GarageViewModel(
                    ServiceLocator.profileRepository,
                    ServiceLocator.garageRepository,
                    ServiceLocator.upgradeCarUseCase
                )

            else -> SplashViewModel()
        }
        return created as T
    }
}
