package dev.koenv.roadassist.server.incidents

import dev.koenv.roadassist.core.auth.AuthResponse
import dev.koenv.roadassist.core.auth.LoginRequest
import dev.koenv.roadassist.core.auth.RegisterRequest
import dev.koenv.roadassist.core.incident.CreateIncidentRequest
import dev.koenv.roadassist.core.incident.Incident
import dev.koenv.roadassist.core.incident.IncidentCategory
import dev.koenv.roadassist.core.incident.IncidentStatus
import dev.koenv.roadassist.core.incident.PatchIncidentStatusRequest
import dev.koenv.roadassist.server.applyTestConfig
import dev.koenv.roadassist.server.database.CommentsTable
import dev.koenv.roadassist.server.database.IncidentsTable
import dev.koenv.roadassist.server.database.RefreshTokensTable
import dev.koenv.roadassist.server.database.UsersTable
import dev.koenv.roadassist.server.module
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
        transaction { SchemaUtils.drop(CommentsTable, RefreshTokensTable, IncidentsTable, UsersTable) }
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
        assertEquals(dev.koenv.roadassist.core.incident.IncidentStatus.NEW, incident.status)
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
            setBody(RegisterRequest("other_user", "pass1234"))
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
            setBody(dev.koenv.roadassist.core.incident.PatchIncidentStatusRequest(
                status = dev.koenv.roadassist.core.incident.IncidentStatus.IN_PROGRESS,
                notes = "On the way",
            ))
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val updated = response.body<Incident>()
        assertEquals(dev.koenv.roadassist.core.incident.IncidentStatus.IN_PROGRESS, updated.status)
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
            setBody(dev.koenv.roadassist.core.incident.PatchIncidentStatusRequest(
                status = dev.koenv.roadassist.core.incident.IncidentStatus.IN_PROGRESS,
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
            setBody(dev.koenv.roadassist.core.incident.PatchIncidentStatusRequest(
                status = dev.koenv.roadassist.core.incident.IncidentStatus.RESOLVED,
                notes = null,
            ))
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun patch_status_with_invalid_value_returns_400() = testApplication {
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
            setBody("""{"status":"NOT_A_REAL_STATUS","notes":null}""")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun owner_can_upload_jpeg_photo() = testApplication {
        applyTestConfig()
        application { module() }
        val client = createClient { install(ClientContentNegotiation) { json() } }
        val token = loginToken(client, "user", "user123")

        val created = client.post("/incidents") {
            headers { append(HttpHeaders.Authorization, "Bearer $token") }
            contentType(ContentType.Application.Json)
            setBody(createBody)
        }.body<Incident>()

        val jpegBytes = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xD9.toByte())
        val response = client.post("/incidents/${created.id}/photo") {
            headers { append(HttpHeaders.Authorization, "Bearer $token") }
            setBody(io.ktor.client.request.forms.MultiPartFormDataContent(
                io.ktor.client.request.forms.formData {
                    append("photo", jpegBytes, io.ktor.http.Headers.build {
                        append(io.ktor.http.HttpHeaders.ContentType, ContentType.Image.JPEG.toString())
                        append(io.ktor.http.HttpHeaders.ContentDisposition, "form-data; name=\"photo\"; filename=\"test.jpg\"")
                    })
                }
            ))
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val updated = response.body<Incident>()
        assertNotNull(updated.photoUrl)
        assertTrue(updated.photoUrl!!.startsWith("/uploads/"))
        val filename = updated.photoUrl!!.removePrefix("/uploads/")
        assertTrue(java.io.File("uploads/$filename").exists())
    }

    @Test
    fun owner_can_upload_png_photo() = testApplication {
        applyTestConfig()
        application { module() }
        val client = createClient { install(ClientContentNegotiation) { json() } }
        val token = loginToken(client, "user", "user123")

        val created = client.post("/incidents") {
            headers { append(HttpHeaders.Authorization, "Bearer $token") }
            contentType(ContentType.Application.Json)
            setBody(createBody)
        }.body<Incident>()

        val pngBytes = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47)
        val response = client.post("/incidents/${created.id}/photo") {
            headers { append(HttpHeaders.Authorization, "Bearer $token") }
            setBody(io.ktor.client.request.forms.MultiPartFormDataContent(
                io.ktor.client.request.forms.formData {
                    append("photo", pngBytes, io.ktor.http.Headers.build {
                        append(io.ktor.http.HttpHeaders.ContentType, ContentType.Image.PNG.toString())
                        append(io.ktor.http.HttpHeaders.ContentDisposition, "form-data; name=\"photo\"; filename=\"test.png\"")
                    })
                }
            ))
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val updated = response.body<Incident>()
        assertNotNull(updated.photoUrl)
        assertTrue(updated.photoUrl!!.startsWith("/uploads/"))
    }

    @Test
    fun non_allowed_type_gif_upload_returns_415() = testApplication {
        applyTestConfig()
        application { module() }
        val client = createClient { install(ClientContentNegotiation) { json() } }
        val token = loginToken(client, "user", "user123")

        val created = client.post("/incidents") {
            headers { append(HttpHeaders.Authorization, "Bearer $token") }
            contentType(ContentType.Application.Json)
            setBody(createBody)
        }.body<Incident>()

        val response = client.post("/incidents/${created.id}/photo") {
            headers { append(HttpHeaders.Authorization, "Bearer $token") }
            setBody(io.ktor.client.request.forms.MultiPartFormDataContent(
                io.ktor.client.request.forms.formData {
                    append("photo", byteArrayOf(0x47, 0x49, 0x46), io.ktor.http.Headers.build {
                        append(io.ktor.http.HttpHeaders.ContentType, "image/gif")
                        append(io.ktor.http.HttpHeaders.ContentDisposition, "form-data; name=\"photo\"; filename=\"anim.gif\"")
                    })
                }
            ))
        }
        assertEquals(HttpStatusCode.UnsupportedMediaType, response.status)
    }

    @Test
    fun photo_over_5mb_returns_413() = testApplication {
        applyTestConfig()
        application { module() }
        val client = createClient { install(ClientContentNegotiation) { json() } }
        val token = loginToken(client, "user", "user123")

        val created = client.post("/incidents") {
            headers { append(HttpHeaders.Authorization, "Bearer $token") }
            contentType(ContentType.Application.Json)
            setBody(createBody)
        }.body<Incident>()

        val oversizedBytes = ByteArray(5 * 1024 * 1024 + 1) { 0x00 }.also {
            it[0] = 0xFF.toByte(); it[1] = 0xD8.toByte(); it[2] = 0xFF.toByte()
        }
        val response = client.post("/incidents/${created.id}/photo") {
            headers { append(HttpHeaders.Authorization, "Bearer $token") }
            setBody(io.ktor.client.request.forms.MultiPartFormDataContent(
                io.ktor.client.request.forms.formData {
                    append("photo", oversizedBytes, io.ktor.http.Headers.build {
                        append(io.ktor.http.HttpHeaders.ContentType, ContentType.Image.JPEG.toString())
                        append(io.ktor.http.HttpHeaders.ContentDisposition, "form-data; name=\"photo\"; filename=\"big.jpg\"")
                    })
                }
            ))
        }
        assertEquals(HttpStatusCode.PayloadTooLarge, response.status)
    }

    @Test
    fun other_user_cannot_upload_photo_returns_403() = testApplication {
        applyTestConfig()
        application { module() }
        val client = createClient { install(ClientContentNegotiation) { json() } }
        val userToken = loginToken(client, "user", "user123")
        client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest("intruder", "pass1234"))
        }
        val intruderToken = loginToken(client, "intruder", "pass1234")

        val created = client.post("/incidents") {
            headers { append(HttpHeaders.Authorization, "Bearer $userToken") }
            contentType(ContentType.Application.Json)
            setBody(createBody)
        }.body<Incident>()

        val response = client.post("/incidents/${created.id}/photo") {
            headers { append(HttpHeaders.Authorization, "Bearer $intruderToken") }
            setBody(io.ktor.client.request.forms.MultiPartFormDataContent(
                io.ktor.client.request.forms.formData {
                    append("photo", byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xD9.toByte()), io.ktor.http.Headers.build {
                        append(io.ktor.http.HttpHeaders.ContentType, ContentType.Image.JPEG.toString())
                        append(io.ktor.http.HttpHeaders.ContentDisposition, "form-data; name=\"photo\"; filename=\"test.jpg\"")
                    })
                }
            ))
        }
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun patch_status_notes_over_1000_chars_returns_422() = testApplication {
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

        // 1001 characters exceeds the 1000-character limit
        val longNotes = "A".repeat(1001)
        val response = client.patch("/incidents/${created.id}/status") {
            headers { append(HttpHeaders.Authorization, "Bearer $dispatcherToken") }
            contentType(ContentType.Application.Json)
            setBody(PatchIncidentStatusRequest(
                status = IncidentStatus.IN_PROGRESS,
                notes = longNotes,
            ))
        }
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
    }

    @Test
    fun patch_status_trims_whitespace_from_notes() = testApplication {
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
            setBody(PatchIncidentStatusRequest(
                status = IncidentStatus.IN_PROGRESS,
                notes = "  On the way  ",
            ))
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val updated = response.body<Incident>()
        // Leading and trailing whitespace must be stripped before storing
        assertEquals("On the way", updated.notes)
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
