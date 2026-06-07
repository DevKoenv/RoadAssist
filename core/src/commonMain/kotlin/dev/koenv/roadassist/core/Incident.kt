package dev.koenv.roadassist.core

import kotlinx.serialization.Serializable

@Serializable
data class Incident(
    val id: Int,
    val userId: Int,
    val category: IncidentCategory,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val photoUrl: String?,
    val status: IncidentStatus,
    val createdAt: String,
    val updatedAt: String,
)
