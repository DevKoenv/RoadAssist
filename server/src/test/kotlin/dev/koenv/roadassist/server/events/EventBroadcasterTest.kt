package dev.koenv.roadassist.server.events

import io.ktor.sse.ServerSentEvent
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class EventBroadcasterTest {

    private val event = ServerSentEvent(event = "INCIDENT_CREATED", data = "{}")

    @Test
    fun dispatcher_receives_event_from_any_owner() = runBlocking {
        val broadcaster = EventBroadcaster()
        val received = mutableListOf<ServerSentEvent>()
        val session = ClientSession(userId = 1, role = "DISPATCHER") { received.add(it) }
        broadcaster.register(session)

        broadcaster.emit(event, ownerUserId = 99)

        assertEquals(1, received.size)
    }

    @Test
    fun road_user_receives_own_event() = runBlocking {
        val broadcaster = EventBroadcaster()
        val received = mutableListOf<ServerSentEvent>()
        val session = ClientSession(userId = 42, role = "ROAD_USER") { received.add(it) }
        broadcaster.register(session)

        broadcaster.emit(event, ownerUserId = 42)

        assertEquals(1, received.size)
    }

    @Test
    fun road_user_does_not_receive_other_users_event() = runBlocking {
        val broadcaster = EventBroadcaster()
        val received = mutableListOf<ServerSentEvent>()
        val session = ClientSession(userId = 42, role = "ROAD_USER") { received.add(it) }
        broadcaster.register(session)

        broadcaster.emit(event, ownerUserId = 99)

        assertEquals(0, received.size)
    }

    @Test
    fun broken_send_does_not_propagate_exception() = runBlocking {
        val broadcaster = EventBroadcaster()
        val session = ClientSession(userId = 1, role = "DISPATCHER") {
            throw RuntimeException("simulated connection error")
        }
        broadcaster.register(session)
        broadcaster.emit(event, ownerUserId = 1)  // must not throw
    }

    @Test
    fun unregistered_session_does_not_receive() = runBlocking {
        val broadcaster = EventBroadcaster()
        val received = mutableListOf<ServerSentEvent>()
        val session = ClientSession(userId = 1, role = "DISPATCHER") { received.add(it) }
        broadcaster.register(session)
        broadcaster.unregister(session)

        broadcaster.emit(event, ownerUserId = 1)

        assertEquals(0, received.size)
    }
}
