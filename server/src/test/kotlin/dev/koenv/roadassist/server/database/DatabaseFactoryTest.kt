package dev.koenv.roadassist.server.database

import dev.koenv.roadassist.core.IncidentCategory
import dev.koenv.roadassist.core.IncidentStatus
import dev.koenv.roadassist.core.Role
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class DatabaseFactoryTest {

    @BeforeTest
    fun setUp() {
        DatabaseFactory.init()
    }

    @AfterTest
    fun tearDown() {
        transaction {
            SchemaUtils.drop(CommentsTable, RefreshTokensTable, IncidentsTable, UsersTable)
        }
    }

    @Test
    fun init_creates_users_and_incidents_tables() {
        transaction {
            assertEquals(0, UsersTable.selectAll().count())
            assertEquals(0, IncidentsTable.selectAll().count())
        }
    }

    @Test
    fun can_insert_user_into_users_table() {
        val userId = transaction {
            UsersTable.insert {
                it[username] = "testuser"
                it[passwordHash] = "hashedpassword"
                it[role] = Role.ROAD_USER
            } get UsersTable.id
        }
        assertNotNull(userId)
    }

    @Test
    fun can_insert_incident_with_user_foreign_key() {
        val insertedUserId = transaction {
            UsersTable.insert {
                it[username] = "incuser"
                it[passwordHash] = "hash"
                it[role] = Role.DISPATCHER
            } get UsersTable.id
        }

        val incidentId = transaction {
            IncidentsTable.insert {
                it[userId] = insertedUserId
                it[category] = IncidentCategory.BREAKDOWN
                it[description] = "Test incident"
                it[latitude] = 51.92
                it[longitude] = 4.48
                it[photoUrl] = null
                it[status] = IncidentStatus.NEW
                it[createdAt] = "2026-06-07T00:00:00Z"
                it[updatedAt] = "2026-06-07T00:00:00Z"
            } get IncidentsTable.id
        }
        assertNotNull(incidentId)
    }

    @Test
    fun init_is_idempotent() {
        DatabaseFactory.init()
        transaction {
            assertEquals(0, UsersTable.selectAll().count())
        }
    }
}
