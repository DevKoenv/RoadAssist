package dev.koenv.roadassist.app.data.sse

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import dev.koenv.roadassist.app.data.api.FakeApiClient
import dev.koenv.roadassist.app.data.incidents.IncidentRepository
import dev.koenv.roadassist.app.data.storage.SecureStorage
import dev.koenv.roadassist.app.db.RoadAssistDb
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EventStreamServiceTest {

    private object FakeStorage : SecureStorage {
        override fun saveToken(token: String) = Unit
        override fun getToken(): String = "token"
        override fun clearToken() = Unit
        override fun saveRefreshToken(token: String) = Unit
        override fun getRefreshToken(): String? = null
        override fun clearRefreshToken() = Unit
    }

    private val service: EventStreamService by lazy {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        RoadAssistDb.Schema.create(driver)
        val db = RoadAssistDb(driver)
        val repository = IncidentRepository(FakeApiClient(), db)
        EventStreamService(FakeStorage, repository)
    }

    companion object {
        private val INCIDENT_JSON = """
            {"id":1,"userId":42,"category":"BREAKDOWN","description":"test","latitude":51.0,"longitude":4.0,"photoUrl":null,"status":"NEW","notes":null,"createdAt":"2026-01-01T00:00:00Z","updatedAt":"2026-01-01T00:00:00Z"}
        """.trimIndent()

        private val COMMENT_JSON = """
            {"id":1,"incidentId":1,"authorRole":"DISPATCHER","type":"MESSAGE","content":"hello","createdAt":"2026-01-01T00:00:00Z"}
        """.trimIndent()
    }

    @Test
    fun parseEvent_incident_created() {
        val event = service.parseEvent("INCIDENT_CREATED", INCIDENT_JSON)
        assertTrue(event is EventStreamService.SseEvent.IncidentCreated)
        assertEquals(1, (event as EventStreamService.SseEvent.IncidentCreated).incident.id)
    }

    @Test
    fun parseEvent_incident_updated() {
        val event = service.parseEvent("INCIDENT_UPDATED", INCIDENT_JSON)
        assertTrue(event is EventStreamService.SseEvent.IncidentUpdated)
    }

    @Test
    fun parseEvent_comment_added() {
        val event = service.parseEvent("COMMENT_ADDED", COMMENT_JSON)
        assertTrue(event is EventStreamService.SseEvent.CommentAdded)
        assertEquals(1, (event as EventStreamService.SseEvent.CommentAdded).comment.id)
    }

    @Test
    fun parseEvent_unknown_type_returns_unknown() {
        val event = service.parseEvent("SOMETHING_ELSE", "{}")
        assertTrue(event is EventStreamService.SseEvent.Unknown)
        assertEquals("SOMETHING_ELSE", (event as EventStreamService.SseEvent.Unknown).type)
    }

    @Test
    fun parseEvent_null_type_returns_unknown() {
        val event = service.parseEvent(null, "{}")
        assertTrue(event is EventStreamService.SseEvent.Unknown)
    }

    @Test
    fun parseEvent_malformed_json_returns_unknown() {
        val event = service.parseEvent("INCIDENT_CREATED", "not-json")
        assertTrue(event is EventStreamService.SseEvent.Unknown)
    }
}
