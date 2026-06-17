package dev.koenv.roadassist.server.events

import com.auth0.jwt.JWT
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.header
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.sse.sse
import io.ktor.sse.ServerSentEvent
import kotlinx.coroutines.delay

fun Route.configureEventsRouting(broadcaster: EventBroadcaster) {
    sse("/events") {
        // call.principal<JWTPrincipal>() is not accessible inside an sse { } handler in Ktor 3.x,
        // so we decode claims directly from the header. The authenticate { } wrapper upstream
        // already verified the signature, so plain decode is safe here.
        val token = call.request.header(HttpHeaders.Authorization)
            ?.removePrefix("Bearer ")
            ?.takeIf { it.isNotBlank() }
        val decoded = token?.let { runCatching { JWT.decode(it) }.getOrNull() }
        val userId = decoded?.subject?.toIntOrNull()
        val role = decoded?.getClaim("role")?.asString()
        if (userId == null || role == null) {
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
