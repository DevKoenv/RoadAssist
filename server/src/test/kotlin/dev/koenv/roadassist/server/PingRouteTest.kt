package dev.koenv.roadassist.server

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.koenv.roadassist.core.PingMessage
import dev.koenv.roadassist.core.Role
import dev.koenv.roadassist.server.database.CommentsTable
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
import java.util.Date
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class PingRouteTest {

    @AfterTest
    fun tearDown() {
        transaction {
            SchemaUtils.drop(CommentsTable, RefreshTokensTable, IncidentsTable, UsersTable)
        }
    }

    private fun testToken(): String = JWT.create()
        .withSubject("1")
        .withClaim("role", Role.ROAD_USER.name)
        .withExpiresAt(Date(System.currentTimeMillis() + 86_400_000L))
        .sign(Algorithm.HMAC256(TEST_JWT_SECRET))

    @Test
    fun `GET ping with valid token returns 200 with pong content`() = testApplication {
        applyTestConfig()
        application { module() }
        val client = createClient { install(ClientContentNegotiation) { json() } }
        val response = client.get("/ping") {
            bearerAuth(testToken())
        }
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("pong", response.body<PingMessage>().content)
    }
}
