package dev.koenv.roadassist

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class PingMessageTest {

    @Test
    fun serializes_and_deserializes_round_trip() {
        val original = PingMessage(content = "hello")
        val encoded = Json.encodeToString(original)
        val decoded = Json.decodeFromString<PingMessage>(encoded)
        assertEquals(original, decoded)
    }
}
