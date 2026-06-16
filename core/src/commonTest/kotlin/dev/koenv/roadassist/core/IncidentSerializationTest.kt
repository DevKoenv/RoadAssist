package dev.koenv.roadassist.core.incident

import dev.koenv.roadassist.core.incident.Incident
import dev.koenv.roadassist.core.incident.IncidentCategory
import dev.koenv.roadassist.core.incident.IncidentStatus
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class IncidentSerializationTest {

    private val sample = Incident(
        id = 1,
        userId = 2,
        category = IncidentCategory.BREAKDOWN,
        description = "Car broke down",
        latitude = 51.9225,
        longitude = 4.47917,
        photoUrl = null,
        status = IncidentStatus.NEW,
        createdAt = "2026-06-07T10:00:00Z",
        updatedAt = "2026-06-07T10:00:00Z",
    )

    @Test
    fun incident_round_trip() {
        val decoded = Json.decodeFromString<Incident>(Json.encodeToString(sample))
        assertEquals(sample, decoded)
    }

    @Test
    fun incident_null_photo_url_round_trip() {
        assertNull(sample.photoUrl)
        val decoded = Json.decodeFromString<Incident>(Json.encodeToString(sample))
        assertNull(decoded.photoUrl)
    }

    @Test
    fun incident_with_photo_url_round_trip() {
        val withPhoto = sample.copy(photoUrl = "https://example.com/photo.jpg")
        val decoded = Json.decodeFromString<Incident>(Json.encodeToString(withPhoto))
        assertEquals("https://example.com/photo.jpg", decoded.photoUrl)
    }

    @Test
    fun incident_status_values_serialize() {
        assertEquals("\"NEW\"", Json.encodeToString(IncidentStatus.NEW))
        assertEquals("\"IN_PROGRESS\"", Json.encodeToString(IncidentStatus.IN_PROGRESS))
        assertEquals("\"EN_ROUTE\"", Json.encodeToString(IncidentStatus.EN_ROUTE))
        assertEquals("\"RESOLVED\"", Json.encodeToString(IncidentStatus.RESOLVED))
    }

    @Test
    fun incident_category_values_serialize() {
        assertEquals("\"BREAKDOWN\"", Json.encodeToString(IncidentCategory.BREAKDOWN))
        assertEquals("\"ACCIDENT\"", Json.encodeToString(IncidentCategory.ACCIDENT))
        assertEquals("\"OBSTRUCTION\"", Json.encodeToString(IncidentCategory.OBSTRUCTION))
        assertEquals("\"OTHER\"", Json.encodeToString(IncidentCategory.OTHER))
    }
}
