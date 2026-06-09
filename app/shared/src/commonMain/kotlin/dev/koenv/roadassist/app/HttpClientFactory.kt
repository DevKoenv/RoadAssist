package dev.koenv.roadassist.app

import io.ktor.client.HttpClient

expect fun createHttpClient(): HttpClient
