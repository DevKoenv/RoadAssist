package dev.koenv.roadassist.app.ui.dispatcher.home

import dev.koenv.roadassist.core.incident.Incident
import dev.koenv.roadassist.core.incident.IncidentCategory
import dev.koenv.roadassist.core.incident.IncidentStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DispatcherFiltersTest {

    private fun incident(
        id: Int,
        status: IncidentStatus,
        category: IncidentCategory,
    ) = Incident(
        id = id, userId = 1, category = category, description = "test",
        latitude = 51.0, longitude = 4.0, photoUrl = null, status = status,
        notes = null, createdAt = "now", updatedAt = "now",
    )

    private val incidents = listOf(
        incident(1, IncidentStatus.NEW, IncidentCategory.BREAKDOWN),
        incident(2, IncidentStatus.NEW, IncidentCategory.ACCIDENT),
        incident(3, IncidentStatus.IN_PROGRESS, IncidentCategory.BREAKDOWN),
        incident(4, IncidentStatus.RESOLVED, IncidentCategory.OBSTRUCTION),
        incident(5, IncidentStatus.EN_ROUTE, IncidentCategory.OTHER),
    )

    @Test
    fun all_all_returns_every_incident() {
        val result = filterIncidents(incidents, DispatcherStatusFilter.All, DispatcherCategoryFilter.All)
        assertEquals(5, result.size)
    }

    @Test
    fun status_filter_alone_returns_matching_subset() {
        val result = filterIncidents(incidents, DispatcherStatusFilter.New, DispatcherCategoryFilter.All)
        assertEquals(2, result.size)
        assertTrue(result.all { it.status == IncidentStatus.NEW })
    }

    @Test
    fun category_filter_alone_returns_matching_subset() {
        val result = filterIncidents(incidents, DispatcherStatusFilter.All, DispatcherCategoryFilter.Breakdown)
        assertEquals(2, result.size)
        assertTrue(result.all { it.category == IncidentCategory.BREAKDOWN })
    }

    @Test
    fun combined_and_filter_returns_intersection() {
        val result = filterIncidents(incidents, DispatcherStatusFilter.New, DispatcherCategoryFilter.Breakdown)
        assertEquals(1, result.size)
        assertEquals(1, result[0].id)
    }

    @Test
    fun combined_filter_with_no_match_returns_empty() {
        val result = filterIncidents(incidents, DispatcherStatusFilter.Resolved, DispatcherCategoryFilter.Accident)
        assertTrue(result.isEmpty())
    }
}
