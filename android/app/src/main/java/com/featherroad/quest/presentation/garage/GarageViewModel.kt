package com.featherroad.quest.presentation.garage

import androidx.lifecycle.ViewModel
import com.featherroad.quest.domain.repository.GarageRepository
import com.featherroad.quest.domain.repository.ProfileRepository
import com.featherroad.quest.domain.usecase.UpgradeCarUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GarageViewModel(
    private val profileRepository: ProfileRepository,
    private val garageRepository: GarageRepository,
    private val upgradeCarUseCase: UpgradeCarUseCase
) : ViewModel() {

    private val mutableState = MutableStateFlow(GarageUiState.empty())
    val state: StateFlow<GarageUiState> = mutableState.asStateFlow()

    private var carsTab = true

    fun refresh() {
        val profile = profileRepository.profile()
        mutableState.value = GarageUiState(
            cars = garageRepository.cars().map { car ->
                CarRow(
                    car = car,
                    level = profile.upgrades[car.id] ?: 0,
                    selected = car.id == profile.selectedCarId
                )
            },
            outfits = garageRepository.outfits().map { outfit ->
                OutfitRow(
                    outfit = outfit,
                    owned = profile.ownedOutfits.contains(outfit.id),
                    selected = outfit.id == profile.selectedOutfitId
                )
            },
            balance = profile.balance,
            paintIndex = profile.paintIndex,
            carsTabActive = carsTab
        )
    }

    fun selectTab(cars: Boolean) {
        carsTab = cars
        refresh()
    }

    fun selectCar(row: CarRow) {
        profileRepository.save(profileRepository.profile().copy(selectedCarId = row.car.id))
        refresh()
    }

    fun selectPaint(index: Int) {
        profileRepository.save(profileRepository.profile().copy(paintIndex = index))
        refresh()
    }

    fun selectOutfit(row: OutfitRow) {
        val profile = profileRepository.profile()
        if (row.owned) {
            profileRepository.save(profile.copy(selectedOutfitId = row.outfit.id))
            refresh()
            return
        }
        if (profile.balance < row.outfit.price) return
        val owned = profile.ownedOutfits.toMutableSet()
        owned.add(row.outfit.id)
        profileRepository.save(
            profile.copy(
                balance = profile.balance - row.outfit.price,
                ownedOutfits = owned,
                selectedOutfitId = row.outfit.id
            )
        )
        refresh()
    }

    fun upgradeSelected(): Boolean {
        val selected = mutableState.value.cars.firstOrNull { it.selected } ?: return false
        val done = upgradeCarUseCase(selected.car)
        refresh()
        return done
    }
}
