package dev.koenv.roadassist.app

actual val BASE_URL: String
    get() = System.getProperty("roadassist.serverUrl", "https://roadassist.koenv.dev")
