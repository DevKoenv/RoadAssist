package dev.koenv.roadassist.app.data.incidents

import dev.koenv.roadassist.app.data.api.ApiClient
import dev.koenv.roadassist.core.CreateIncidentRequest
import dev.koenv.roadassist.core.Incident
import dev.koenv.roadassist.core.PatchIncidentStatusRequest

class IncidentRepository(private val apiClient: ApiClient) {

    suspend fun createIncident(request: CreateIncidentRequest): Result<Incident> =
        apiClient.createIncident(request)

    suspend fun getIncidents(): Result<List<Incident>> =
        apiClient.getIncidents()

    suspend fun getIncident(id: Int): Result<Incident> =
        apiClient.getIncident(id)

    suspend fun patchIncidentStatus(id: Int, request: PatchIncidentStatusRequest): Result<Incident> =
        apiClient.patchIncidentStatus(id, request)

    suspend fun uploadPhoto(incidentId: Int, imageBytes: ByteArray, mimeType: String): Result<Incident> =
        apiClient.uploadPhoto(incidentId, imageBytes, mimeType)
}
