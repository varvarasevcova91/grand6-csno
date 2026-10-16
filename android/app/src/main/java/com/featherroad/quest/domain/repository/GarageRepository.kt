package com.featherroad.quest.domain.repository

import com.featherroad.quest.domain.model.CarModel
import com.featherroad.quest.domain.model.OutfitModel

interface GarageRepository {

    fun cars(): List<CarModel>

    fun outfits(): List<OutfitModel>

    fun carById(id: String): CarModel
}
