package dev.koenv.roadassist.server.database

import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

object DatabaseFactory {

    fun init() {
        connect()
        transaction {
            SchemaUtils.create(UsersTable, IncidentsTable)
        }
    }

    private fun connect() {
        val dbMode = System.getenv("DB_MODE") ?: "h2"
        when (dbMode.lowercase()) {
            "sqlite" -> {
                val dbPath = System.getenv("DB_PATH")
                    ?: error("DB_PATH environment variable must be set when DB_MODE=sqlite")
                Database.connect(
                    url = "jdbc:sqlite:$dbPath",
                    driver = "org.sqlite.JDBC",
                )
            }
            else -> Database.connect(
                url = "jdbc:h2:mem:roadassist;DB_CLOSE_DELAY=-1",
                driver = "org.h2.Driver",
            )
        }
    }
}
