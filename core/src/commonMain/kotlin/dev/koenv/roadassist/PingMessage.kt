package dev.koenv.roadassist

import kotlinx.serialization.Serializable

@Serializable
data class PingMessage(val content: String)
