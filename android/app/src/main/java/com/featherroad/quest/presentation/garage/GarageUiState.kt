package com.featherroad.quest.presentation.garage

data class GarageUiState(
    val cars: List<CarRow>,
    val outfits: List<OutfitRow>,
    val balance: Int,
    val paintIndex: Int,
    val carsTabActive: Boolean
) {
    companion object {
        fun empty(): GarageUiState = GarageUiState(
            cars = emptyList(),
            outfits = emptyList(),
            balance = 0,
            paintIndex = 0,
            carsTabActive = true
        )
    }
}
