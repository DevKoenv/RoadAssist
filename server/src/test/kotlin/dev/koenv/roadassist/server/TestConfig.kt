package dev.koenv.roadassist.server

import io.ktor.server.config.*
import io.ktor.server.testing.*

internal const val TEST_JWT_SECRET = "test-jwt-secret-do-not-use-in-production"

internal fun ApplicationTestBuilder.applyTestConfig() {
    environment {
        config = MapApplicationConfig(
            "database.mode" to "h2",
            "jwt.secret" to TEST_JWT_SECRET,
        )
    }
}
