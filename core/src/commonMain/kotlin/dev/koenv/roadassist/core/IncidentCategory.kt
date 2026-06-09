package dev.koenv.roadassist.core

import kotlinx.serialization.Serializable

@Serializable
enum class IncidentCategory { BREAKDOWN, ACCIDENT, OBSTRUCTION, OTHER }
