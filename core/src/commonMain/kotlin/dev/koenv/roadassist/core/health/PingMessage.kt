package dev.koenv.roadassist.core.health

import kotlinx.serialization.Serializable

@Serializable
data class PingMessage(val content: String)
