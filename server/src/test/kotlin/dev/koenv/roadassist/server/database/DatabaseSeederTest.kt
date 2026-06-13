package dev.koenv.roadassist.server.database

import dev.koenv.roadassist.core.Role
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DatabaseSeederTest {

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
    fun seed_creates_road_user_account() {
        DatabaseSeeder.seed()
        val row = transaction {
            UsersTable.selectAll().where { UsersTable.username eq "user" }.firstOrNull()
        }
        assertNotNull(row)
        assertEquals(Role.ROAD_USER, row[UsersTable.role])
    }

    @Test
    fun seed_creates_dispatcher_account() {
        DatabaseSeeder.seed()
        val row = transaction {
            UsersTable.selectAll().where { UsersTable.username eq "dispatcher" }.firstOrNull()
        }
        assertNotNull(row)
        assertEquals(Role.DISPATCHER, row[UsersTable.role])
    }

    @Test
    fun seed_stores_passwords_as_bcrypt_hashes() {
        DatabaseSeeder.seed()
        transaction {
            val userHash = UsersTable.selectAll()
                .where { UsersTable.username eq "user" }.first()[UsersTable.passwordHash]
            val dispatcherHash = UsersTable.selectAll()
                .where { UsersTable.username eq "dispatcher" }.first()[UsersTable.passwordHash]
            assertTrue(BCrypt.checkpw("user123", userHash))
            assertTrue(BCrypt.checkpw("dispatch123", dispatcherHash))
        }
    }

    @Test
    fun seed_is_idempotent() {
        DatabaseSeeder.seed()
        DatabaseSeeder.seed()
        val count = transaction { UsersTable.selectAll().count() }
        assertEquals(2L, count)
    }
}
