package dev.koenv.roadassist.core.incident

import kotlinx.serialization.Serializable

@Serializable
enum class IncidentCategory { BREAKDOWN, ACCIDENT, OBSTRUCTION, OTHER }

fun IncidentCategory.displayName(): String = name.lowercase().replaceFirstChar { it.uppercase() }
