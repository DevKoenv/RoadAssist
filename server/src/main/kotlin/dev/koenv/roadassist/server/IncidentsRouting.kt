package dev.koenv.roadassist.server

import dev.koenv.roadassist.core.Incident
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

fun Route.configureIncidentsRouting() {
    get("/incidents") {
        call.respond(emptyList<Incident>())
    }
}
