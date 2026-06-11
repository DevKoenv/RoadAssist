package dev.koenv.roadassist.app.data.incidents

import dev.koenv.roadassist.app.data.api.ApiClient
import dev.koenv.roadassist.core.CreateIncidentRequest
import dev.koenv.roadassist.core.Incident

class IncidentRepository(private val apiClient: ApiClient) {
    suspend fun createIncident(request: CreateIncidentRequest): Result<Incident> =
        apiClient.createIncident(request)

    suspend fun uploadPhoto(incidentId: Int, imageBytes: ByteArray, mimeType: String): Result<Incident> =
        apiClient.uploadPhoto(incidentId, imageBytes, mimeType)
}
