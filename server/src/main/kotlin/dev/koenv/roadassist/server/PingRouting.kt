package dev.koenv.roadassist.server

import dev.koenv.roadassist.core.health.PingMessage
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

fun Route.configurePingRouting() {
    get("/ping") {
        call.respond(PingMessage(content = "pong"))
    }
}
