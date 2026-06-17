package dev.koenv.roadassist.server.events

import dev.koenv.roadassist.core.user.Role
import io.ktor.sse.ServerSentEvent
import java.util.concurrent.CopyOnWriteArrayList

data class ClientSession(
    val userId: Int,
    val role: String,
    val send: suspend (ServerSentEvent) -> Unit,
)

class EventBroadcaster {
    private val sessions = CopyOnWriteArrayList<ClientSession>()

    fun register(session: ClientSession) { sessions.add(session) }
    fun unregister(session: ClientSession) { sessions.remove(session) }

    suspend fun emit(event: ServerSentEvent, ownerUserId: Int) {
        for (session in sessions) {
            val shouldReceive = session.role == Role.DISPATCHER.name || session.userId == ownerUserId
            if (shouldReceive) {
                runCatching { session.send(event) }
            }
        }
    }
}
