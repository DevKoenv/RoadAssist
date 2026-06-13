package dev.koenv.roadassist.server.database

import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

object DatabaseFactory {

    fun init(mode: String = "h2", path: String? = null) {
        connect(mode, path)
        transaction {
            SchemaUtils.create(UsersTable, IncidentsTable, RefreshTokensTable, CommentsTable)
        }
    }

    private fun connect(mode: String, path: String?) {
        when (mode.lowercase()) {
            "sqlite" -> {
                val dbPath = path ?: error("database.path must be set when database.mode is sqlite")
                Database.connect(
                    url = "jdbc:sqlite:$dbPath",
                    driver = "org.sqlite.JDBC",
                )
            }
            // Default to H2 in-memory; data resets on restart, used for local dev/testing
            else -> Database.connect(
                url = "jdbc:h2:mem:roadassist;DB_CLOSE_DELAY=-1",
                driver = "org.h2.Driver",
            )
        }
    }
}
