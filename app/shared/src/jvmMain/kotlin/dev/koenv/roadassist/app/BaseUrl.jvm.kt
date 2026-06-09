package dev.koenv.roadassist.app

actual val BASE_URL: String
    get() = System.getProperty("roadassist.serverUrl", "http://localhost:8080")
