package dev.koenv.roadassist.app.ui.dispatcher.home

import dev.koenv.roadassist.core.incident.Incident
import dev.koenv.roadassist.core.incident.IncidentCategory
import dev.koenv.roadassist.core.incident.IncidentStatus

internal enum class DispatcherStatusFilter(val displayName: String) {
    New("New"),
    InProgress("In progress"),
    EnRoute("En route"),
    Resolved("Resolved"),
}

internal enum class DispatcherCategoryFilter(val displayName: String) {
    Breakdown("Breakdown"),
    Accident("Accident"),
    Obstruction("Obstruction"),
    Other("Other"),
}

internal fun filterIncidents(
    incidents: List<Incident>,
    statusFilters: Set<DispatcherStatusFilter>,
    categoryFilters: Set<DispatcherCategoryFilter>,
): List<Incident> {
    val byStatus = if (statusFilters.isEmpty()) {
        incidents
    } else {
        incidents.filter { inc ->
            statusFilters.any { f ->
                when (f) {
                    DispatcherStatusFilter.New -> inc.status == IncidentStatus.NEW
                    DispatcherStatusFilter.InProgress -> inc.status == IncidentStatus.IN_PROGRESS
                    DispatcherStatusFilter.EnRoute -> inc.status == IncidentStatus.EN_ROUTE
                    DispatcherStatusFilter.Resolved -> inc.status == IncidentStatus.RESOLVED
                }
            }
        }
    }
    return if (categoryFilters.isEmpty()) {
        byStatus
    } else {
        byStatus.filter { inc ->
            categoryFilters.any { f ->
                when (f) {
                    DispatcherCategoryFilter.Breakdown -> inc.category == IncidentCategory.BREAKDOWN
                    DispatcherCategoryFilter.Accident -> inc.category == IncidentCategory.ACCIDENT
                    DispatcherCategoryFilter.Obstruction -> inc.category == IncidentCategory.OBSTRUCTION
                    DispatcherCategoryFilter.Other -> inc.category == IncidentCategory.OTHER
                }
            }
        }
    }
}
