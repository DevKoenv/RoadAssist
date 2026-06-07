package dev.koenv.roadassist.core

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class AuthSerializationTest {

    @Test
    fun login_request_round_trip() {
        val original = LoginRequest(username = "alice", password = "secret")
        val decoded = Json.decodeFromString<LoginRequest>(Json.encodeToString(original))
        assertEquals(original, decoded)
    }

    @Test
    fun auth_response_round_trip() {
        val original = AuthResponse(token = "eyJ.abc.def", role = Role.DISPATCHER)
        val decoded = Json.decodeFromString<AuthResponse>(Json.encodeToString(original))
        assertEquals(original, decoded)
    }

    @Test
    fun login_request_fields_map_correctly() {
        val json = """{"username":"bob","password":"hunter2"}"""
        val req = Json.decodeFromString<LoginRequest>(json)
        assertEquals("bob", req.username)
        assertEquals("hunter2", req.password)
    }

    @Test
    fun auth_response_fields_map_correctly() {
        val json = """{"token":"tok","role":"ROAD_USER"}"""
        val resp = Json.decodeFromString<AuthResponse>(json)
        assertEquals("tok", resp.token)
        assertEquals(Role.ROAD_USER, resp.role)
    }
}
