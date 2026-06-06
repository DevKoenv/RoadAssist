package dev.koenv.roadassist.core

import kotlinx.serialization.Serializable

@Serializable
data class PingMessage(val content: String)
