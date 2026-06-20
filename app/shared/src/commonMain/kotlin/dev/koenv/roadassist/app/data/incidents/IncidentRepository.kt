package dev.koenv.roadassist.app.data.incidents

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import dev.koenv.roadassist.app.data.api.ApiClient
import dev.koenv.roadassist.app.data.db.toDomain
import dev.koenv.roadassist.app.data.db.upsert
import dev.koenv.roadassist.app.db.RoadAssistDb
import dev.koenv.roadassist.core.comment.Comment
import dev.koenv.roadassist.core.incident.CreateIncidentRequest
import dev.koenv.roadassist.core.incident.Incident
import dev.koenv.roadassist.core.incident.PatchIncidentStatusRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class IncidentRepository(
    private val apiClient: ApiClient,
    private val db: RoadAssistDb,
) {
    fun observeIncidents(): Flow<List<Incident>> =
        db.incidentEntityQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows -> rows.map { it.toDomain() } }

    fun observeIncident(id: Int): Flow<Incident?> =
        db.incidentEntityQueries.selectById(id.toLong())
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it?.toDomain() }

    fun observeComments(incidentId: Int): Flow<List<Comment>> =
        db.commentEntityQueries.selectByIncidentId(incidentId.toLong())
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows -> rows.map { it.toDomain() } }

    suspend fun syncIncidents(): Result<Unit> =
        apiClient.getIncidents().map { incidents ->
            db.incidentEntityQueries.transaction {
                // Delete and re-insert rather than upsert alone; handles incidents removed on the server
                db.incidentEntityQueries.deleteAll()
                incidents.forEach { db.incidentEntityQueries.upsert(it) }
            }
        }

    suspend fun syncIncident(id: Int): Result<Unit> =
        apiClient.getIncident(id).map { incident ->
            db.incidentEntityQueries.upsert(incident)
            apiClient.getComments(id).onSuccess { comments ->
                db.commentEntityQueries.transaction {
                    // Replace all comments atomically so deleted server-side comments don't linger
                    db.commentEntityQueries.deleteByIncidentId(id.toLong())
                    comments.forEach { db.commentEntityQueries.upsert(it) }
                }
            }
        }

    suspend fun patchIncidentStatus(id: Int, request: PatchIncidentStatusRequest): Result<Incident> =
        apiClient.patchIncidentStatus(id, request).onSuccess { updated ->
            db.incidentEntityQueries.upsert(updated)
        }

    suspend fun postComment(incidentId: Int, content: String): Result<Comment> =
        apiClient.postComment(incidentId, content).onSuccess { comment ->
            db.commentEntityQueries.upsert(comment)
        }

    suspend fun createIncident(request: CreateIncidentRequest): Result<Incident> =
        apiClient.createIncident(request)

    suspend fun uploadPhoto(incidentId: Int, imageBytes: ByteArray, mimeType: String): Result<Incident> =
        apiClient.uploadPhoto(incidentId, imageBytes, mimeType).onSuccess { updated ->
            db.incidentEntityQueries.upsert(updated)
        }

    suspend fun checkConnectivity(): Boolean = apiClient.checkConnectivity()

    fun upsertIncident(incident: Incident) {
        db.incidentEntityQueries.upsert(incident)
    }

    fun upsertComment(comment: Comment) {
        db.commentEntityQueries.upsert(comment)
    }
}
