package com.featherroad.quest.data.repository

import com.featherroad.quest.data.sample.SampleData
import com.featherroad.quest.domain.model.CarModel
import com.featherroad.quest.domain.model.OutfitModel
import com.featherroad.quest.domain.repository.GarageRepository

class GarageRepositoryImpl : GarageRepository {

    override fun cars(): List<CarModel> = SampleData.cars

    override fun outfits(): List<OutfitModel> = SampleData.outfits

    override fun carById(id: String): CarModel {
        return SampleData.cars.firstOrNull { it.id == id } ?: SampleData.cars[0]
    }
}
