package dev.koenv.roadassist.server.database

import dev.koenv.roadassist.core.user.Role
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.mindrot.jbcrypt.BCrypt

object DatabaseSeeder {

    fun seed() {
        seedAccount("user", "user123", Role.ROAD_USER)
        seedAccount("dispatcher", "dispatch123", Role.DISPATCHER)
    }

    private fun seedAccount(username: String, password: String, role: Role) {
        transaction {
            if (UsersTable.selectAll().where { UsersTable.username eq username }.count() > 0L) return@transaction
            UsersTable.insert {
                it[UsersTable.username] = username
                it[passwordHash] = BCrypt.hashpw(password, BCrypt.gensalt())
                it[UsersTable.role] = role
            }
        }
    }
}
