package dev.koenv.roadassist.core

import kotlinx.serialization.Serializable

@Serializable
data class RefreshRequest(val refreshToken: String)
