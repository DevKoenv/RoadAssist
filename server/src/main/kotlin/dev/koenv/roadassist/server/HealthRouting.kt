package dev.koenv.roadassist.server

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

fun Route.configureHealthRouting() {
    get("/health") {
        call.respond(HttpStatusCode.OK)
    }
}
