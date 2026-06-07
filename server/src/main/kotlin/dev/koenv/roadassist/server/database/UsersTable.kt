package dev.koenv.roadassist.server.database

import dev.koenv.roadassist.core.Role
import org.jetbrains.exposed.dao.id.IntIdTable

object UsersTable : IntIdTable("users") {
    val username = varchar("username", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val role = enumerationByName<Role>("role", 50)
}
