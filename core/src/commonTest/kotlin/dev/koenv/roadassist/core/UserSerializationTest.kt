package dev.koenv.roadassist.core

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class UserSerializationTest {

    @Test
    fun role_serializes_to_string() {
        assertEquals("\"ROAD_USER\"", Json.encodeToString(Role.ROAD_USER))
        assertEquals("\"DISPATCHER\"", Json.encodeToString(Role.DISPATCHER))
    }

    @Test
    fun user_round_trip() {
        val original = User(id = 1, username = "alice", role = Role.ROAD_USER)
        val decoded = Json.decodeFromString<User>(Json.encodeToString(original))
        assertEquals(original, decoded)
    }

    @Test
    fun user_fields_map_correctly() {
        val json = """{"id":2,"username":"bob","role":"DISPATCHER"}"""
        val user = Json.decodeFromString<User>(json)
        assertEquals(2, user.id)
        assertEquals("bob", user.username)
        assertEquals(Role.DISPATCHER, user.role)
    }
}
