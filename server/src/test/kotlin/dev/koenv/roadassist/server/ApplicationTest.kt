package dev.koenv.roadassist.server

import dev.koenv.roadassist.server.database.CommentsTable
import dev.koenv.roadassist.server.database.IncidentsTable
import dev.koenv.roadassist.server.database.RefreshTokensTable
import dev.koenv.roadassist.server.database.UsersTable
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

    @AfterTest
    fun tearDown() {
        transaction {
            SchemaUtils.drop(CommentsTable, RefreshTokensTable, IncidentsTable, UsersTable)
        }
    }

    @Test
    fun testUnknownRouteReturns404() = testApplication {
        applyTestConfig()
        application { module() }
        val response = client.get("/")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }
}
