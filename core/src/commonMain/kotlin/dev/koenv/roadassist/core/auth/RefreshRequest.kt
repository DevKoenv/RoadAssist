package dev.koenv.roadassist.core.auth

import kotlinx.serialization.Serializable

@Serializable
data class RefreshRequest(val refreshToken: String)
