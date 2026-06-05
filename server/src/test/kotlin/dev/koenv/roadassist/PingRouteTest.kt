package dev.koenv.roadassist

import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation as ServerContentNegotiation
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class PingRouteTest {

    @Test
    fun `GET ping returns 200 with pong content`() = testApplication {
        install(ServerContentNegotiation) { json() }
        application { configurePingRouting() }
        val client = createClient {
            install(ClientContentNegotiation) { json() }
        }
        val response = client.get("/ping")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("pong", response.body<PingMessage>().content)
    }
}
