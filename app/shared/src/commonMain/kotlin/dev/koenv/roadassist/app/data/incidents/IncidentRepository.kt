package dev.koenv.roadassist.app.data.incidents

import dev.koenv.roadassist.app.data.api.ApiClient
import dev.koenv.roadassist.core.Comment
import dev.koenv.roadassist.core.CreateIncidentRequest
import dev.koenv.roadassist.core.Incident
import dev.koenv.roadassist.core.PatchIncidentStatusRequest

class IncidentRepository(
    private val apiClient: ApiClient,
    private val cache: LocalIncidentCache,
) {

    suspend fun createIncident(request: CreateIncidentRequest): Result<Incident> =
        apiClient.createIncident(request)

    suspend fun getIncidents(): Result<List<Incident>> =
        apiClient.getIncidents().onSuccess { cache.save(it) }

    fun loadCached(): List<Incident> = cache.load()

    suspend fun checkConnectivity(): Boolean = apiClient.checkConnectivity()

    suspend fun getIncident(id: Int): Result<Incident> =
        apiClient.getIncident(id)

    suspend fun patchIncidentStatus(id: Int, request: PatchIncidentStatusRequest): Result<Incident> =
        apiClient.patchIncidentStatus(id, request)

    suspend fun uploadPhoto(incidentId: Int, imageBytes: ByteArray, mimeType: String): Result<Incident> =
        apiClient.uploadPhoto(incidentId, imageBytes, mimeType)

    suspend fun getComments(incidentId: Int): Result<List<Comment>> =
        apiClient.getComments(incidentId)

    suspend fun postComment(incidentId: Int, content: String): Result<Comment> =
        apiClient.postComment(incidentId, content)
}
