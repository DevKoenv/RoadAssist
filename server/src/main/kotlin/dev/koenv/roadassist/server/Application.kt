package dev.koenv.roadassist.server

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.koenv.roadassist.server.database.DatabaseFactory
import dev.koenv.roadassist.server.database.DatabaseSeeder
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val jwtSecret = System.getenv("JWT_SECRET")
        ?: error("JWT_SECRET environment variable must be set")
    DatabaseFactory.init()
    DatabaseSeeder.seed()
    configure(jwtSecret)
}

internal fun Application.configure(jwtSecret: String) {
    install(Authentication) {
        jwt("auth-jwt") {
            verifier(JWT.require(Algorithm.HMAC256(jwtSecret)).build())
            validate { credential ->
                if (credential.payload.subject != null) JWTPrincipal(credential.payload) else null
            }
        }
    }
    install(ContentNegotiation) { json() }
    routing {
        configureAuthRouting(jwtSecret)
        authenticate("auth-jwt") {
            configurePingRouting()
        }
    }
}
