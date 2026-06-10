package dev.koenv.roadassist.server

import dev.koenv.roadassist.core.AuthResponse
import dev.koenv.roadassist.core.LoginRequest
import dev.koenv.roadassist.core.RefreshRequest
import dev.koenv.roadassist.core.RegisterRequest
import dev.koenv.roadassist.core.Role
import dev.koenv.roadassist.server.database.IncidentsTable
import dev.koenv.roadassist.server.database.RefreshTokensTable
import dev.koenv.roadassist.server.database.UsersTable
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthRoutingTest {

    @AfterTest
    fun tearDown() {
        transaction {
            SchemaUtils.drop(RefreshTokensTable, IncidentsTable, UsersTable)
        }
    }

    @Test
    fun register_with_new_username_returns_201_and_tokens() = testApplication {
        applyTestConfig()
        application { module() }
        val client = createClient { install(ClientContentNegotiation) { json() } }
        val response = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest(username = "newdriver", password = "pass1234"))
        }
        assertEquals(HttpStatusCode.Created, response.status)
        val body = response.body<AuthResponse>()
        assertEquals(Role.ROAD_USER, body.role)
        assertTrue(body.token.isNotBlank())
        assertTrue(body.refreshToken.isNotBlank())
    }

    @Test
    fun register_with_duplicate_username_returns_409() = testApplication {
        applyTestConfig()
        application { module() }
        val client = createClient { install(ClientContentNegotiation) { json() } }
        val response = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest(username = "user", password = "anything"))
        }
        assertEquals(HttpStatusCode.Conflict, response.status)
    }

    @Test
    fun login_with_valid_credentials_returns_200_and_both_tokens() = testApplication {
        applyTestConfig()
        application { module() }
        val client = createClient { install(ClientContentNegotiation) { json() } }
        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(username = "user", password = "user123"))
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<AuthResponse>()
        assertEquals(Role.ROAD_USER, body.role)
        assertTrue(body.token.isNotBlank())
        assertTrue(body.refreshToken.isNotBlank())
    }

    @Test
    fun login_with_wrong_password_returns_401() = testApplication {
        applyTestConfig()
        application { module() }
        val client = createClient { install(ClientContentNegotiation) { json() } }
        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(username = "user", password = "wrongpassword"))
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun login_with_unknown_user_returns_401() = testApplication {
        applyTestConfig()
        application { module() }
        val client = createClient { install(ClientContentNegotiation) { json() } }
        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(username = "nobody", password = "anything"))
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun secured_route_without_token_returns_401() = testApplication {
        applyTestConfig()
        application { module() }
        val response = client.get("/incidents")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun refresh_with_valid_token_returns_new_access_token() = testApplication {
        applyTestConfig()
        application { module() }
        val client = createClient { install(ClientContentNegotiation) { json() } }
        val loginResp = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(username = "user", password = "user123"))
        }.body<AuthResponse>()

        val refreshResp = client.post("/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(RefreshRequest(refreshToken = loginResp.refreshToken))
        }
        assertEquals(HttpStatusCode.OK, refreshResp.status)
        val body = refreshResp.body<AuthResponse>()
        assertTrue(body.token.isNotBlank())
        assertEquals(loginResp.refreshToken, body.refreshToken)
    }

    @Test
    fun refresh_with_invalid_token_returns_401() = testApplication {
        applyTestConfig()
        application { module() }
        val client = createClient { install(ClientContentNegotiation) { json() } }
        val response = client.post("/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(RefreshRequest(refreshToken = "not-a-real-token"))
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun logout_then_refresh_returns_401() = testApplication {
        applyTestConfig()
        application { module() }
        val client = createClient { install(ClientContentNegotiation) { json() } }
        val loginResp = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(username = "user", password = "user123"))
        }.body<AuthResponse>()

        val logoutResp = client.post("/auth/logout") {
            contentType(ContentType.Application.Json)
            setBody(RefreshRequest(refreshToken = loginResp.refreshToken))
        }
        assertEquals(HttpStatusCode.NoContent, logoutResp.status)

        val refreshResp = client.post("/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(RefreshRequest(refreshToken = loginResp.refreshToken))
        }
        assertEquals(HttpStatusCode.Unauthorized, refreshResp.status)
    }
}
