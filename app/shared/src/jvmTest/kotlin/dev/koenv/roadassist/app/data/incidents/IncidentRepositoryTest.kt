package dev.koenv.roadassist.app.data.incidents

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import dev.koenv.roadassist.app.data.incidents.IncidentRepository
import dev.koenv.roadassist.app.data.api.FakeApiClient
import dev.koenv.roadassist.app.db.RoadAssistDb
import dev.koenv.roadassist.core.comment.AuthorRole
import dev.koenv.roadassist.core.comment.Comment
import dev.koenv.roadassist.core.comment.CommentType
import dev.koenv.roadassist.core.incident.CreateIncidentRequest
import dev.koenv.roadassist.core.incident.Incident
import dev.koenv.roadassist.core.incident.IncidentCategory
import dev.koenv.roadassist.core.incident.IncidentStatus
import dev.koenv.roadassist.core.incident.PatchIncidentStatusRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class IncidentRepositoryTest {

    private lateinit var db: RoadAssistDb
    private lateinit var repository: IncidentRepository

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        RoadAssistDb.Schema.create(driver)
        db = RoadAssistDb(driver)
        repository = IncidentRepository(FakeApiClient(), db)
    }

    @AfterTest
    fun cleanup() {
        Dispatchers.resetMain()
    }

    private fun incident(id: Int = 1, status: IncidentStatus = IncidentStatus.NEW) = Incident(
        id = id, userId = 42, category = IncidentCategory.BREAKDOWN,
        description = "test", latitude = 51.0, longitude = 4.0,
        photoUrl = null, status = status, notes = null,
        createdAt = "2026-01-01T00:00:00Z", updatedAt = "2026-01-01T00:00:00Z",
    )

    private fun comment(id: Int = 1, incidentId: Int = 1) = Comment(
        id = id, incidentId = incidentId, authorRole = AuthorRole.DISPATCHER,
        type = CommentType.MESSAGE, content = "hello",
        createdAt = "2026-01-01T00:00:00Z",
    )

    @Test
    fun observeIncidents_empty_on_fresh_db() = runTest {
        val result = repository.observeIncidents().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun observeIncident_null_when_not_cached() = runTest {
        val result = repository.observeIncident(99).first()
        assertNull(result)
    }

    @Test
    fun observeComments_empty_when_none_cached() = runTest {
        val result = repository.observeComments(1).first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun syncIncidents_writes_api_results_to_db() = runTest {
        val incident = incident(id = 1)
        val api = FakeApiClient(getIncidentsResult = Result.success(listOf(incident)))
        val repo = IncidentRepository(api, db)

        repo.syncIncidents()

        val stored = repo.observeIncidents().first()
        assertEquals(1, stored.size)
        assertEquals(incident, stored[0])
    }

    @Test
    fun syncIncidents_replaces_existing_data() = runTest {
        val first = incident(id = 1, status = IncidentStatus.NEW)
        val updated = first.copy(status = IncidentStatus.IN_PROGRESS)
        val api1 = FakeApiClient(getIncidentsResult = Result.success(listOf(first)))
        val api2 = FakeApiClient(getIncidentsResult = Result.success(listOf(updated)))
        val repo1 = IncidentRepository(api1, db)
        val repo2 = IncidentRepository(api2, db)

        repo1.syncIncidents()
        repo2.syncIncidents()

        val stored = repo2.observeIncidents().first()
        assertEquals(IncidentStatus.IN_PROGRESS, stored[0].status)
    }

    @Test
    fun syncIncident_writes_incident_and_comments_to_db() = runTest {
        val inc = incident(id = 5)
        val com = comment(id = 10, incidentId = 5)
        val api = FakeApiClient(
            getIncidentResult = Result.success(inc),
            getCommentsResult = Result.success(listOf(com)),
        )
        val repo = IncidentRepository(api, db)

        repo.syncIncident(5)

        assertEquals(inc, repo.observeIncident(5).first())
        assertEquals(listOf(com), repo.observeComments(5).first())
    }

    @Test
    fun patchIncidentStatus_writes_updated_incident_to_db() = runTest {
        val inc = incident(id = 1, status = IncidentStatus.NEW)
        val updated = inc.copy(status = IncidentStatus.IN_PROGRESS)
        val api = FakeApiClient(
            getIncidentsResult = Result.success(listOf(inc)),
            patchResult = Result.success(updated),
        )
        val repo = IncidentRepository(api, db)
        repo.syncIncidents()

        repo.patchIncidentStatus(1, PatchIncidentStatusRequest(IncidentStatus.IN_PROGRESS, null))

        assertEquals(IncidentStatus.IN_PROGRESS, repo.observeIncident(1).first()?.status)
    }

    @Test
    fun createIncident_returns_api_result_on_success() = runTest {
        val expected = incident(id = 5)
        val request = CreateIncidentRequest(IncidentCategory.ACCIDENT, "rear-end collision", 51.5, 4.1)
        val api = FakeApiClient(createIncidentResult = Result.success(expected))
        val repo = IncidentRepository(api, db)

        val result = repo.createIncident(request)

        assertEquals(expected, result.getOrNull())
    }

    @Test
    fun createIncident_propagates_api_failure() = runTest {
        val repo = IncidentRepository(FakeApiClient(), db)

        val result = repo.createIncident(
            CreateIncidentRequest(IncidentCategory.BREAKDOWN, "breakdown", 51.0, 4.0)
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun postComment_writes_comment_to_db() = runTest {
        val com = comment(id = 1, incidentId = 1)
        val api = FakeApiClient(postCommentResult = Result.success(com))
        val repo = IncidentRepository(api, db)

        repo.postComment(1, "hello")

        assertEquals(listOf(com), repo.observeComments(1).first())
    }
}
