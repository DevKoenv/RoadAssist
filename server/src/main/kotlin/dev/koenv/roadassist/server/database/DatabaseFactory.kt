package dev.koenv.roadassist.server.database

import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

/**
 * Initialises the Exposed database connection and creates all tables on first run.
 *
 * Supports two backends, selected via the `DB_MODE` environment variable:
 * - `sqlite`: persistent file database at the path given by `DB_PATH`. Suitable for production.
 * - `h2` (default): in-memory H2 database. Data is lost on restart. Suitable for local dev and CI.
 *
 * [SchemaUtils.create] is idempotent: it runs `CREATE TABLE IF NOT EXISTS` statements so existing
 * tables are never dropped or modified when the server restarts.
 */
object DatabaseFactory {

    /**
     * Connects to the database and ensures all required tables exist.
     *
     * @param mode `"sqlite"` for a persistent database or any other value for H2 in-memory.
     * @param path Filesystem path for the SQLite file. Must be non-null when [mode] is `"sqlite"`.
     */
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
            // Default to H2 in-memory; data resets on restart, used for local dev/testing.
            // DB_CLOSE_DELAY=-1 keeps the schema alive for the JVM lifetime; without it H2
            // drops the in-memory DB as soon as the last connection closes.
            else -> Database.connect(
                url = "jdbc:h2:mem:roadassist;DB_CLOSE_DELAY=-1",
                driver = "org.h2.Driver",
            )
        }
    }
}
