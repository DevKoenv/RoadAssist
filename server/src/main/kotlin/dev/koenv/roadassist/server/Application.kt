package dev.koenv.roadassist.server

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.koenv.roadassist.server.auth.configureAuthRouting
import dev.koenv.roadassist.server.database.DatabaseFactory
import dev.koenv.roadassist.server.database.DatabaseSeeder
import dev.koenv.roadassist.server.health.configureHealthRouting
import dev.koenv.roadassist.server.health.configurePingRouting
import dev.koenv.roadassist.server.incidents.configureIncidentsRouting
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.http.content.staticFiles
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import org.slf4j.event.Level

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
    val config = environment.config
    // Env vars take precedence over application.conf so prod deployments don't need a config file change
    val jwtSecret = System.getenv("JWT_SECRET")
        ?: config.propertyOrNull("jwt.secret")?.getString()
        ?: error("JWT_SECRET must be set via the JWT_SECRET environment variable")
    val dbMode = System.getenv("DB_MODE")
        ?: config.propertyOrNull("database.mode")?.getString()
        ?: "h2"  // default to H2 in-memory for local dev; nothing to configure
    val dbPath = System.getenv("DB_PATH")
        ?: config.propertyOrNull("database.path")?.getString()
    DatabaseFactory.init(dbMode, dbPath)
    DatabaseSeeder.seed()
    configure(jwtSecret)
}

private fun Application.configure(jwtSecret: String) {
    install(Authentication) {
        jwt("auth-jwt") {
            verifier(JWT.require(Algorithm.HMAC256(jwtSecret)).build())
            validate { credential ->
                if (credential.payload.subject != null) JWTPrincipal(credential.payload) else null
            }
        }
    }
    install(CallLogging) { level = Level.INFO }
    install(ContentNegotiation) { json() }
    routing {
        java.io.File("uploads").mkdirs()  // created relative to CWD; must exist before static serving
        staticFiles("/uploads", java.io.File("uploads"))
        configureHealthRouting()
        configurePingRouting()
        configureAuthRouting(jwtSecret)
        authenticate("auth-jwt") {
            configureIncidentsRouting()
        }
    }
}
