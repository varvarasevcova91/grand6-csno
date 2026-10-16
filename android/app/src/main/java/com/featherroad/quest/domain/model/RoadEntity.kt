package com.featherroad.quest.domain.model

data class RoadEntity(
    val id: Long,
    val kind: EntityKind,
    val lane: Int,
    val y: Float
)
