package dev.koenv.roadassist.server.events

import dev.koenv.roadassist.server.auth.jwtClaims
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.sse.sse
import io.ktor.sse.ServerSentEvent
import kotlinx.coroutines.delay

fun Route.configureEventsRouting(broadcaster: EventBroadcaster) {
    sse("/events") {
        val (userId, role) = call.jwtClaims() ?: run {
            call.respond(HttpStatusCode.Unauthorized)
            return@sse
        }
        val session = ClientSession(userId, role) { event -> send(event) }
        broadcaster.register(session)
        try {
            while (true) {
                delay(30_000L)
                send(ServerSentEvent(comments = "heartbeat"))
            }
        } finally {
            broadcaster.unregister(session)
        }
    }
}
