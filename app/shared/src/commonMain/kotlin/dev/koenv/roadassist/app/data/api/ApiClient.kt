package dev.koenv.roadassist.app.data.api

import dev.koenv.roadassist.core.Comment
import dev.koenv.roadassist.core.auth.AuthResponse
import dev.koenv.roadassist.core.auth.LoginRequest
import dev.koenv.roadassist.core.auth.RefreshRequest
import dev.koenv.roadassist.core.incident.CreateIncidentRequest
import dev.koenv.roadassist.core.incident.Incident
import dev.koenv.roadassist.core.incident.PatchIncidentStatusRequest

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
    suspend fun getComments(incidentId: Int): Result<List<Comment>>
    suspend fun postComment(incidentId: Int, content: String): Result<Comment>
}
