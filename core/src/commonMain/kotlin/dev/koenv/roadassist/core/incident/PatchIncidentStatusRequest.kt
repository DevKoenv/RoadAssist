package dev.koenv.roadassist.core.incident

import kotlinx.serialization.Serializable

@Serializable
data class PatchIncidentStatusRequest(
    val status: IncidentStatus,
    val notes: String?,
)
