package dev.koenv.roadassist.core

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(val token: String, val role: Role)
