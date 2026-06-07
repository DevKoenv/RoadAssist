package dev.koenv.roadassist.core

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val username: String, val password: String)
