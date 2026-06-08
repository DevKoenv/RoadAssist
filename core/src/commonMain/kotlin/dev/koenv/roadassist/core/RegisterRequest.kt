package dev.koenv.roadassist.core

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(val username: String, val password: String)
