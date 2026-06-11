package dev.koenv.roadassist.server

import dev.koenv.roadassist.core.AuthResponse
import dev.koenv.roadassist.core.CreateIncidentRequest
import dev.koenv.roadassist.core.Incident
import dev.koenv.roadassist.core.IncidentCategory
import dev.koenv.roadassist.core.LoginRequest
import dev.koenv.roadassist.server.database.IncidentsTable
import dev.koenv.roadassist.server.database.RefreshTokensTable
import dev.koenv.roadassist.server.database.UsersTable
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class IncidentsRoutingTest {

    @AfterTest
    fun tearDown() {
        transaction { SchemaUtils.drop(RefreshTokensTable, IncidentsTable, UsersTable) }
        java.io.File("uploads").listFiles()?.forEach { it.delete() }
    }

    private val createBody = CreateIncidentRequest(
        category = IncidentCategory.BREAKDOWN,
        description = "Engine failure",
        latitude = 51.9225,
        longitude = 4.47917,
    )

    @Test
    fun road_user_can_create_incident_returns_201() = testApplication {
        applyTestConfig()
        application { module() }
        val client = createClient { install(ClientContentNegotiation) { json() } }
        val token = loginToken(client, "user", "user123")

        val response = client.post("/incidents") {
            headers { append(HttpHeaders.Authorization, "Bearer $token") }
            contentType(ContentType.Application.Json)
            setBody(createBody)
        }
        assertEquals(HttpStatusCode.Created, response.status)
        val incident = response.body<Incident>()
        assertEquals(IncidentCategory.BREAKDOWN, incident.category)
        assertEquals("Engine failure", incident.description)
        assertEquals(dev.koenv.roadassist.core.IncidentStatus.NEW, incident.status)
    }

    @Test
    fun dispatcher_cannot_create_incident_returns_403() = testApplication {
        applyTestConfig()
        application { module() }
        val client = createClient { install(ClientContentNegotiation) { json() } }
        val token = loginToken(client, "dispatcher", "dispatch123")

        val response = client.post("/incidents") {
            headers { append(HttpHeaders.Authorization, "Bearer $token") }
            contentType(ContentType.Application.Json)
            setBody(createBody)
        }
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun unauthenticated_create_returns_401() = testApplication {
        applyTestConfig()
        application { module() }
        val client = createClient { install(ClientContentNegotiation) { json() } }
        val response = client.post("/incidents") {
            contentType(ContentType.Application.Json)
            setBody(createBody)
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun road_user_get_incidents_returns_own_only() = testApplication {
        applyTestConfig()
        application { module() }
        val client = createClient { install(ClientContentNegotiation) { json() } }
        val userToken = loginToken(client, "user", "user123")

        client.post("/incidents") {
            headers { append(HttpHeaders.Authorization, "Bearer $userToken") }
            contentType(ContentType.Application.Json)
            setBody(createBody)
        }

        val response = client.get("/incidents") {
            headers { append(HttpHeaders.Authorization, "Bearer $userToken") }
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val list = response.body<List<Incident>>()
        assertEquals(1, list.size)
        assertEquals("Engine failure", list[0].description)
    }

    @Test
    fun dispatcher_get_incidents_returns_all() = testApplication {
        applyTestConfig()
        application { module() }
        val client = createClient { install(ClientContentNegotiation) { json() } }
        val userToken = loginToken(client, "user", "user123")
        val dispatcherToken = loginToken(client, "dispatcher", "dispatch123")

        client.post("/incidents") {
            headers { append(HttpHeaders.Authorization, "Bearer $userToken") }
            contentType(ContentType.Application.Json)
            setBody(createBody)
        }

        val response = client.get("/incidents") {
            headers { append(HttpHeaders.Authorization, "Bearer $dispatcherToken") }
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val list = response.body<List<Incident>>()
        assertTrue(list.any { it.description == "Engine failure" })
    }

    @Test
    fun get_incident_by_id_owner_returns_200() = testApplication {
        applyTestConfig()
        application { module() }
        val client = createClient { install(ClientContentNegotiation) { json() } }
        val token = loginToken(client, "user", "user123")

        val created = client.post("/incidents") {
            headers { append(HttpHeaders.Authorization, "Bearer $token") }
            contentType(ContentType.Application.Json)
            setBody(createBody)
        }.body<Incident>()

        val response = client.get("/incidents/${created.id}") {
            headers { append(HttpHeaders.Authorization, "Bearer $token") }
        }
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(created.id, response.body<Incident>().id)
    }

    @Test
    fun get_incident_by_id_wrong_user_returns_403() = testApplication {
        applyTestConfig()
        application { module() }
        val client = createClient { install(ClientContentNegotiation) { json() } }
        val userToken = loginToken(client, "user", "user123")

        client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(dev.koenv.roadassist.core.RegisterRequest("other_user", "pass1234"))
        }
        val otherToken = loginToken(client, "other_user", "pass1234")
        val created = client.post("/incidents") {
            headers { append(HttpHeaders.Authorization, "Bearer $otherToken") }
            contentType(ContentType.Application.Json)
            setBody(createBody)
        }.body<Incident>()

        val response = client.get("/incidents/${created.id}") {
            headers { append(HttpHeaders.Authorization, "Bearer $userToken") }
        }
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun dispatcher_can_get_any_incident() = testApplication {
        applyTestConfig()
        application { module() }
        val client = createClient { install(ClientContentNegotiation) { json() } }
        val userToken = loginToken(client, "user", "user123")
        val dispatcherToken = loginToken(client, "dispatcher", "dispatch123")

        val created = client.post("/incidents") {
            headers { append(HttpHeaders.Authorization, "Bearer $userToken") }
            contentType(ContentType.Application.Json)
            setBody(createBody)
        }.body<Incident>()

        val response = client.get("/incidents/${created.id}") {
            headers { append(HttpHeaders.Authorization, "Bearer $dispatcherToken") }
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun dispatcher_can_patch_incident_status() = testApplication {
        applyTestConfig()
        application { module() }
        val client = createClient { install(ClientContentNegotiation) { json() } }
        val userToken = loginToken(client, "user", "user123")
        val dispatcherToken = loginToken(client, "dispatcher", "dispatch123")

        val created = client.post("/incidents") {
            headers { append(HttpHeaders.Authorization, "Bearer $userToken") }
            contentType(ContentType.Application.Json)
            setBody(createBody)
        }.body<Incident>()

        val response = client.patch("/incidents/${created.id}/status") {
            headers { append(HttpHeaders.Authorization, "Bearer $dispatcherToken") }
            contentType(ContentType.Application.Json)
            setBody(dev.koenv.roadassist.core.PatchIncidentStatusRequest(
                status = dev.koenv.roadassist.core.IncidentStatus.IN_PROGRESS,
                notes = "On the way",
            ))
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val updated = response.body<Incident>()
        assertEquals(dev.koenv.roadassist.core.IncidentStatus.IN_PROGRESS, updated.status)
        assertEquals("On the way", updated.notes)
    }

    @Test
    fun road_user_cannot_patch_status_returns_403() = testApplication {
        applyTestConfig()
        application { module() }
        val client = createClient { install(ClientContentNegotiation) { json() } }
        val userToken = loginToken(client, "user", "user123")

        val created = client.post("/incidents") {
            headers { append(HttpHeaders.Authorization, "Bearer $userToken") }
            contentType(ContentType.Application.Json)
            setBody(createBody)
        }.body<Incident>()

        val response = client.patch("/incidents/${created.id}/status") {
            headers { append(HttpHeaders.Authorization, "Bearer $userToken") }
            contentType(ContentType.Application.Json)
            setBody(dev.koenv.roadassist.core.PatchIncidentStatusRequest(
                status = dev.koenv.roadassist.core.IncidentStatus.IN_PROGRESS,
                notes = null,
            ))
        }
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun patch_status_unknown_id_returns_404() = testApplication {
        applyTestConfig()
        application { module() }
        val client = createClient { install(ClientContentNegotiation) { json() } }
        val dispatcherToken = loginToken(client, "dispatcher", "dispatch123")

        val response = client.patch("/incidents/99999/status") {
            headers { append(HttpHeaders.Authorization, "Bearer $dispatcherToken") }
            contentType(ContentType.Application.Json)
            setBody(dev.koenv.roadassist.core.PatchIncidentStatusRequest(
                status = dev.koenv.roadassist.core.IncidentStatus.RESOLVED,
                notes = null,
            ))
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }
}

private suspend fun loginToken(
    client: io.ktor.client.HttpClient,
    username: String,
    password: String,
): String = client.post("/auth/login") {
    contentType(ContentType.Application.Json)
    setBody(LoginRequest(username, password))
}.body<AuthResponse>().token
