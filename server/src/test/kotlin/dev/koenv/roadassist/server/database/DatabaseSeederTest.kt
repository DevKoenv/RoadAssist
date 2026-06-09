package dev.koenv.roadassist.server.database

import dev.koenv.roadassist.core.Role
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.insert
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
            SchemaUtils.drop(RefreshTokensTable, IncidentsTable, UsersTable)
        }
    }

    @Test
    fun seed_creates_user_account() {
        DatabaseSeeder.seed()
        val row = transaction { UsersTable.selectAll().firstOrNull() }
        assertNotNull(row)
        assertEquals("user", row[UsersTable.username])
        assertEquals(Role.ROAD_USER, row[UsersTable.role])
    }

    @Test
    fun seed_stores_password_as_bcrypt_hash() {
        DatabaseSeeder.seed()
        val hash = transaction { UsersTable.selectAll().first()[UsersTable.passwordHash] }
        assertTrue(BCrypt.checkpw("user123", hash))
    }

    @Test
    fun seed_is_idempotent() {
        DatabaseSeeder.seed()
        DatabaseSeeder.seed()
        val count = transaction { UsersTable.selectAll().count() }
        assertEquals(1L, count)
    }

    @Test
    fun seed_skips_when_users_already_exist() {
        transaction {
            UsersTable.insert {
                it[username] = "existing"
                it[passwordHash] = "hash"
                it[role] = Role.ROAD_USER
            }
        }
        DatabaseSeeder.seed()
        val count = transaction { UsersTable.selectAll().count() }
        assertEquals(1L, count)
    }
}
