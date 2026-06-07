package dev.koenv.roadassist.server

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

    @Test
    fun testUnknownRouteReturns404() = testApplication {
        application { configure("test-jwt-secret-application-test-32c") }
        val response = client.get("/")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }
}
