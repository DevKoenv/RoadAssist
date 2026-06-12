package dev.koenv.roadassist.app

import dev.koenv.roadassist.app.data.api.ApiClient
import dev.koenv.roadassist.app.data.api.ApiException
import dev.koenv.roadassist.core.AuthResponse
import dev.koenv.roadassist.core.CreateIncidentRequest
import dev.koenv.roadassist.core.Incident
import dev.koenv.roadassist.core.LoginRequest
import dev.koenv.roadassist.core.PatchIncidentStatusRequest
import dev.koenv.roadassist.core.RefreshRequest

class FakeApiClient(
    private val loginResult: Result<AuthResponse> = Result.failure(ApiException.Unauthorized()),
    private val createIncidentResult: Result<Incident> = Result.failure(
        ApiException.Network(RuntimeException("not configured"))
    ),
    private val getIncidentsResult: Result<List<Incident>> = Result.success(emptyList()),
) : ApiClient {
    override suspend fun login(request: LoginRequest): Result<AuthResponse> = loginResult
    override suspend fun refresh(request: RefreshRequest): Result<AuthResponse> =
        Result.failure(ApiException.Unauthorized())
    override suspend fun logout(request: RefreshRequest) {}
    override suspend fun checkConnectivity(): Boolean = true
    override suspend fun createIncident(request: CreateIncidentRequest): Result<Incident> = createIncidentResult
    override suspend fun getIncidents(): Result<List<Incident>> = getIncidentsResult
    override suspend fun getIncident(id: Int): Result<Incident> =
        Result.failure(ApiException.Network(RuntimeException("not configured")))
    override suspend fun patchIncidentStatus(id: Int, request: PatchIncidentStatusRequest): Result<Incident> =
        Result.failure(ApiException.Network(RuntimeException("not configured")))
    override suspend fun uploadPhoto(incidentId: Int, imageBytes: ByteArray, mimeType: String): Result<Incident> =
        Result.failure(ApiException.Network(RuntimeException("not configured")))
}
