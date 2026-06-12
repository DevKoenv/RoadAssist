package dev.koenv.roadassist.app.data.api

import dev.koenv.roadassist.core.AuthResponse
import dev.koenv.roadassist.core.CreateIncidentRequest
import dev.koenv.roadassist.core.Incident
import dev.koenv.roadassist.core.LoginRequest
import dev.koenv.roadassist.core.PatchIncidentStatusRequest
import dev.koenv.roadassist.core.RefreshRequest

interface ApiClient {
    suspend fun login(request: LoginRequest): Result<AuthResponse>
    suspend fun refresh(request: RefreshRequest): Result<AuthResponse>
    suspend fun logout(request: RefreshRequest)
    suspend fun checkConnectivity(): Boolean
    suspend fun createIncident(request: CreateIncidentRequest): Result<Incident>
    suspend fun getIncidents(): Result<List<Incident>>
    suspend fun getIncident(id: Int): Result<Incident>
    suspend fun patchIncidentStatus(id: Int, request: PatchIncidentStatusRequest): Result<Incident>
    suspend fun uploadPhoto(incidentId: Int, imageBytes: ByteArray, mimeType: String): Result<Incident>
}
