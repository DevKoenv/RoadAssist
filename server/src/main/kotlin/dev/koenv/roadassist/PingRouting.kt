package dev.koenv.roadassist

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configurePingRouting() {
    routing {
        get("/ping") {
            call.respond(PingMessage(content = "pong"))
        }
    }
}
