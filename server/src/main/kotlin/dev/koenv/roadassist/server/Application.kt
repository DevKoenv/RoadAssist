package dev.koenv.roadassist.server

import dev.koenv.roadassist.server.database.DatabaseFactory
import dev.koenv.roadassist.server.database.DatabaseSeeder
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    DatabaseFactory.init()
    DatabaseSeeder.seed()
    install(ContentNegotiation) { json() }
    configurePingRouting()
}
