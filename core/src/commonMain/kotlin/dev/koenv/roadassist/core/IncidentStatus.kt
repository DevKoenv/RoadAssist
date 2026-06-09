package dev.koenv.roadassist.core

import kotlinx.serialization.Serializable

@Serializable
enum class IncidentStatus { NEW, IN_PROGRESS, EN_ROUTE, RESOLVED }
