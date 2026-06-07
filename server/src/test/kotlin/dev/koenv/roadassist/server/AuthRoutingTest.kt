package dev.koenv.roadassist.server

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.koenv.roadassist.core.AuthResponse
import dev.koenv.roadassist.core.LoginRequest
import dev.koenv.roadassist.core.Role
import dev.koenv.roadassist.server.database.DatabaseFactory
import dev.koenv.roadassist.server.database.DatabaseSeeder
import dev.koenv.roadassist.server.database.IncidentsTable
import dev.koenv.roadassist.server.database.UsersTable
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.Date
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthRoutingTest {

    private val testSecret = "test-jwt-secret-for-auth-routing-tests-32chars"

    @BeforeTest
    fun setUp() {
        DatabaseFactory.init()
        DatabaseSeeder.seed()
    }

    @AfterTest
    fun tearDown() {
        transaction {
            SchemaUtils.drop(IncidentsTable, UsersTable)
        }
    }

    @Test
    fun login_with_valid_credentials_returns_200_and_auth_response() = testApplication {
        application { configure(testSecret) }
        val client = createClient { install(ClientContentNegotiation) { json() } }
        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(username = "user", password = "user123"))
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<AuthResponse>()
        assertEquals(Role.ROAD_USER, body.role)
        assertTrue(body.token.isNotBlank())
    }

    @Test
    fun login_with_wrong_password_returns_401() = testApplication {
        application { configure(testSecret) }
        val client = createClient { install(ClientContentNegotiation) { json() } }
        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(username = "user", password = "wrongpassword"))
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun login_with_unknown_user_returns_401() = testApplication {
        application { configure(testSecret) }
        val client = createClient { install(ClientContentNegotiation) { json() } }
        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(username = "nobody", password = "anything"))
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun secured_route_without_token_returns_401() = testApplication {
        application { configure(testSecret) }
        val response = client.get("/ping")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}
