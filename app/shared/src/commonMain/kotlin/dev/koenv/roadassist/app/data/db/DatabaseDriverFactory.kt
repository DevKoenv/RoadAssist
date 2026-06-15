package dev.koenv.roadassist.app.data.db

import app.cash.sqldelight.db.SqlDriver

expect fun createDatabaseDriver(): SqlDriver
