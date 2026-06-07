package dev.koenv.roadassist.server

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.koenv.roadassist.server.database.DatabaseFactory
import dev.koenv.roadassist.server.database.DatabaseSeeder
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
    val config = environment.config
    val jwtSecret = config.propertyOrNull("jwt.secret")?.getString()
        ?: System.getenv("JWT_SECRET")
        ?: error("JWT_SECRET must be set via config or environment variable")
    val dbMode = config.property("database.mode").getString()
    val dbPath = config.propertyOrNull("database.path")?.getString()
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
    install(ContentNegotiation) { json() }
    routing {
        configureAuthRouting(jwtSecret)
        authenticate("auth-jwt") {
            configurePingRouting()
        }
    }
}
