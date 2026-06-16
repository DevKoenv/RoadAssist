package dev.koenv.roadassist.core.incident

import kotlinx.serialization.Serializable

@Serializable
enum class IncidentStatus { NEW, IN_PROGRESS, EN_ROUTE, RESOLVED }

fun IncidentStatus.displayName(): String = when (this) {
    IncidentStatus.NEW -> "New"
    IncidentStatus.IN_PROGRESS -> "In progress"
    IncidentStatus.EN_ROUTE -> "En route"
    IncidentStatus.RESOLVED -> "Resolved"
}
