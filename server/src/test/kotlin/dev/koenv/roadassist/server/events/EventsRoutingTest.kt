package dev.koenv.roadassist.server.events

import dev.koenv.roadassist.server.applyTestConfig
import dev.koenv.roadassist.server.database.CommentsTable
import dev.koenv.roadassist.server.database.IncidentsTable
import dev.koenv.roadassist.server.database.RefreshTokensTable
import dev.koenv.roadassist.server.database.UsersTable
import dev.koenv.roadassist.server.module
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class EventsRoutingTest {

    @AfterTest
    fun tearDown() {
        transaction {
            SchemaUtils.drop(CommentsTable, RefreshTokensTable, IncidentsTable, UsersTable)
        }
    }

    @Test
    fun events_without_auth_returns_401() = testApplication {
        applyTestConfig()
        application { module() }
        val client = createClient { }
        assertEquals(HttpStatusCode.Unauthorized, client.get("/events").status)
    }
}
