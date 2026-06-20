package dev.koenv.roadassist.app.data.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import dev.koenv.roadassist.app.data.storage.AndroidContextHolder
import dev.koenv.roadassist.app.db.RoadAssistDb

actual fun createDatabaseDriver(): SqlDriver =
    AndroidSqliteDriver(
        schema = RoadAssistDb.Schema,
        context = AndroidContextHolder.applicationContext,
        name = "roadassist.db",
    )
