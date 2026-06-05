package dev.koenv.roadassist

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform