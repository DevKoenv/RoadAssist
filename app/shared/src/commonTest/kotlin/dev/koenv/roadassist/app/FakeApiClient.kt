package dev.koenv.roadassist.app

import dev.koenv.roadassist.core.AuthResponse
import dev.koenv.roadassist.core.LoginRequest
import dev.koenv.roadassist.core.RefreshRequest

class FakeApiClient(
    private val loginResult: Result<AuthResponse>,
) : ApiClient {
    override suspend fun login(request: LoginRequest): Result<AuthResponse> = loginResult
    override suspend fun refresh(request: RefreshRequest): Result<AuthResponse> = Result.failure(ApiException.Unauthorized())
    override suspend fun logout(request: RefreshRequest) {}
    override suspend fun checkConnectivity(): Boolean = true
}
