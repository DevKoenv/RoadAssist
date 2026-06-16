package dev.koenv.roadassist.app.data.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import dev.koenv.roadassist.app.db.RoadAssistDb
import java.nio.file.Path
import kotlin.io.path.createDirectories

actual fun createDatabaseDriver(): SqlDriver {
    // roadassist.storageDir lets tests or custom installs redirect the DB; fallback is ~/.roadassist
    val dir = System.getProperty("roadassist.storageDir")
        ?.let { Path.of(it) }
        ?: Path.of(System.getProperty("user.home"), ".roadassist")
    dir.createDirectories()
    val driver = JdbcSqliteDriver("jdbc:sqlite:${dir.resolve("roadassist.db")}")
    RoadAssistDb.Schema.create(driver)  // generates IF NOT EXISTS DDL, safe on every start
    return driver
}
