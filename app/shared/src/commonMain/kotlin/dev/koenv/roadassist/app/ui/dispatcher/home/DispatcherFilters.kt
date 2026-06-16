package dev.koenv.roadassist.app.ui.dispatcher.home

import dev.koenv.roadassist.core.incident.Incident
import dev.koenv.roadassist.core.incident.IncidentCategory
import dev.koenv.roadassist.core.incident.IncidentStatus

internal enum class DispatcherStatusFilter { All, New, InProgress, EnRoute, Resolved }

internal enum class DispatcherCategoryFilter { All, Breakdown, Accident, Obstruction, Other }

internal fun filterIncidents(
    incidents: List<Incident>,
    statusFilter: DispatcherStatusFilter,
    categoryFilter: DispatcherCategoryFilter,
): List<Incident> {
    val byStatus = when (statusFilter) {
        DispatcherStatusFilter.All -> incidents
        DispatcherStatusFilter.New -> incidents.filter { it.status == IncidentStatus.NEW }
        DispatcherStatusFilter.InProgress -> incidents.filter { it.status == IncidentStatus.IN_PROGRESS }
        DispatcherStatusFilter.EnRoute -> incidents.filter { it.status == IncidentStatus.EN_ROUTE }
        DispatcherStatusFilter.Resolved -> incidents.filter { it.status == IncidentStatus.RESOLVED }
    }
    return when (categoryFilter) {
        DispatcherCategoryFilter.All -> byStatus
        DispatcherCategoryFilter.Breakdown -> byStatus.filter { it.category == IncidentCategory.BREAKDOWN }
        DispatcherCategoryFilter.Accident -> byStatus.filter { it.category == IncidentCategory.ACCIDENT }
        DispatcherCategoryFilter.Obstruction -> byStatus.filter { it.category == IncidentCategory.OBSTRUCTION }
        DispatcherCategoryFilter.Other -> byStatus.filter { it.category == IncidentCategory.OTHER }
    }
}
