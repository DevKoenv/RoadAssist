package dev.koenv.roadassist.app

import dev.koenv.roadassist.core.AuthResponse
import dev.koenv.roadassist.core.LoginRequest
import dev.koenv.roadassist.core.RefreshRequest

interface ApiClient {
    suspend fun login(request: LoginRequest): Result<AuthResponse>
    suspend fun refresh(request: RefreshRequest): Result<AuthResponse>
    suspend fun logout(request: RefreshRequest)
    suspend fun checkConnectivity(): Boolean
}
