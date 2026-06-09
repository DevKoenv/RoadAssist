package dev.koenv.roadassist.server.database

import dev.koenv.roadassist.core.Role
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.mindrot.jbcrypt.BCrypt

object DatabaseSeeder {

    fun seed() {
        transaction {
            if (UsersTable.selectAll().count() > 0L) return@transaction
            UsersTable.insert {
                it[username] = "user"
                it[passwordHash] = BCrypt.hashpw("user123", BCrypt.gensalt())
                it[role] = Role.ROAD_USER
            }
        }
    }
}
