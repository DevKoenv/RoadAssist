package dev.koenv.roadassist.core

import kotlinx.serialization.Serializable

@Serializable
data class CreateIncidentRequest(
    val category: IncidentCategory,
    val description: String,
    val latitude: Double,
    val longitude: Double,
)
