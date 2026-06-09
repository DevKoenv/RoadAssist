package dev.koenv.roadassist.app

actual val BASE_URL: String
    get() = System.getProperty("roadassist.serverUrl", "http://10.10.5.1:8080")
