package dev.koenv.roadassist.core.auth

import dev.koenv.roadassist.core.Role
import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(val token: String, val refreshToken: String, val role: Role)
