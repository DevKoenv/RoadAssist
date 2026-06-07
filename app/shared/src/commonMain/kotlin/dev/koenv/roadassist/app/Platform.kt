package dev.koenv.roadassist.app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
