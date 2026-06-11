package dev.koenv.roadassist.server

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.koenv.roadassist.server.database.DatabaseFactory
import dev.koenv.roadassist.server.database.DatabaseSeeder
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import org.slf4j.event.Level

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
    val config = environment.config
    val jwtSecret = System.getenv("JWT_SECRET")
        ?: config.propertyOrNull("jwt.secret")?.getString()
        ?: error("JWT_SECRET must be set via the JWT_SECRET environment variable")
    val dbMode = System.getenv("DB_MODE")
        ?: config.propertyOrNull("database.mode")?.getString()
        ?: "h2"
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
        configureHealthRouting()
        configurePingRouting()
        configureAuthRouting(jwtSecret)
        authenticate("auth-jwt") {
            configureIncidentsRouting()
        }
    }
}
