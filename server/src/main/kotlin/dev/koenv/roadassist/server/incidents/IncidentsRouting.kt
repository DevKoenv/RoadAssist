package dev.koenv.roadassist.server.incidents

import dev.koenv.roadassist.core.comment.AuthorRole
import dev.koenv.roadassist.core.comment.Comment
import dev.koenv.roadassist.core.comment.CommentType
import dev.koenv.roadassist.core.comment.PostCommentRequest
import dev.koenv.roadassist.core.incident.CreateIncidentRequest
import dev.koenv.roadassist.core.incident.Incident
import dev.koenv.roadassist.core.incident.IncidentStatus
import dev.koenv.roadassist.core.user.Role
import dev.koenv.roadassist.server.auth.jwtClaims
import dev.koenv.roadassist.server.database.CommentsTable
import dev.koenv.roadassist.server.database.IncidentsTable
import dev.koenv.roadassist.server.database.UsersTable
import dev.koenv.roadassist.server.events.EventBroadcaster
import dev.koenv.roadassist.server.events.commentAdded
import dev.koenv.roadassist.server.events.incidentCreated
import dev.koenv.roadassist.server.events.incidentUpdated
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.utils.io.toByteArray
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update

fun Route.configureIncidentsRouting(broadcaster: EventBroadcaster) {
    route("/incidents") {
        post { handleCreateIncident(call, broadcaster) }
        get { handleListIncidents(call) }
        route("/{id}") {
            get { handleGetIncident(call) }
            patch("/status") { handlePatchStatus(call, broadcaster) }
            post("/photo") { handleUploadPhoto(call, broadcaster) }
            get("/comments") { handleListComments(call) }
            post("/comments") { handlePostComment(call, broadcaster) }
        }
    }
}

private suspend fun handleCreateIncident(call: ApplicationCall, broadcaster: EventBroadcaster) {
    val (userId, role) = call.jwtClaims() ?: return call.respond(HttpStatusCode.Unauthorized)
    if (role != Role.ROAD_USER.name) {
        call.respond(HttpStatusCode.Forbidden)
        return
    }
    val body = call.receive<CreateIncidentRequest>()
    val now = java.time.Instant.now().toString()
    val incident = transaction {
        val id = IncidentsTable.insert {
            it[IncidentsTable.userId] = EntityID(userId, UsersTable)
            it[category] = body.category
            it[description] = body.description
            it[latitude] = body.latitude
            it[longitude] = body.longitude
            it[status] = IncidentStatus.NEW
            it[notes] = null
            it[photoUrl] = null
            it[createdAt] = now
            it[updatedAt] = now
        } get IncidentsTable.id
        Incident(
            id = id.value,
            userId = userId,
            category = body.category,
            description = body.description,
            latitude = body.latitude,
            longitude = body.longitude,
            photoUrl = null,
            status = IncidentStatus.NEW,
            notes = null,
            createdAt = now,
            updatedAt = now,
        )
    }
    call.respond(HttpStatusCode.Created, incident)
    broadcaster.emit(incidentCreated(incident), incident.userId)
}

private suspend fun handleListIncidents(call: ApplicationCall) {
    val (userId, role) = call.jwtClaims() ?: return call.respond(HttpStatusCode.Unauthorized)
    val incidents = transaction {
        // Dispatchers see all incidents; road users see only their own
        val query = if (role == Role.DISPATCHER.name) {
            IncidentsTable.selectAll()
        } else {
            IncidentsTable.selectAll().where { IncidentsTable.userId eq EntityID(userId, UsersTable) }
        }
        query.orderBy(IncidentsTable.createdAt to SortOrder.DESC).map { it.toIncident() }
    }
    call.respond(incidents)
}

private suspend fun handleGetIncident(call: ApplicationCall) {
    val (userId, role) = call.jwtClaims() ?: return call.respond(HttpStatusCode.Unauthorized)
    val incidentId = call.parameters["id"]?.toIntOrNull()
        ?: return call.respond(HttpStatusCode.BadRequest)
    val incident = transaction {
        IncidentsTable.selectAll()
            .where { IncidentsTable.id eq incidentId }
            .firstOrNull()?.toIncident()
    } ?: return call.respond(HttpStatusCode.NotFound)
    if (role != Role.DISPATCHER.name && incident.userId != userId) {
        call.respond(HttpStatusCode.Forbidden)
        return
    }
    call.respond(incident)
}

private suspend fun handlePatchStatus(call: ApplicationCall, broadcaster: EventBroadcaster) {
    val (_, role) = call.jwtClaims() ?: return call.respond(HttpStatusCode.Unauthorized)
    if (role != Role.DISPATCHER.name) {
        call.respond(HttpStatusCode.Forbidden)
        return
    }
    val incidentId = call.parameters["id"]?.toIntOrNull()
        ?: return call.respond(HttpStatusCode.BadRequest)
    val body = runCatching {
        call.receive<dev.koenv.roadassist.core.incident.PatchIncidentStatusRequest>()
    }.getOrElse {
        call.respondText("Invalid status value", status = HttpStatusCode.BadRequest)
        return
    }
    // Trim whitespace before validation so the length check reflects the real stored value
    val trimmedNotes = body.notes?.trim()
    if (trimmedNotes != null && trimmedNotes.length > 1000) {
        call.respondText("Notes must not exceed 1000 characters", status = HttpStatusCode.UnprocessableEntity)
        return
    }
    val now = java.time.Instant.now().toString()
    data class PatchResult(val incident: Incident, val comment: Comment)
    val result = transaction {
        IncidentsTable.selectAll()
            .where { IncidentsTable.id eq incidentId }
            .firstOrNull() ?: return@transaction null
        IncidentsTable.update({ IncidentsTable.id eq incidentId }) {
            it[status] = body.status
            trimmedNotes?.let { n -> it[notes] = n }
            it[updatedAt] = now
        }
        // Every status change is automatically audit-logged as a STATUS_CHANGE comment
        val commentId = CommentsTable.insert {
            it[CommentsTable.incidentId] = EntityID(incidentId, IncidentsTable)
            it[CommentsTable.authorRole] = AuthorRole.DISPATCHER
            it[CommentsTable.type] = CommentType.STATUS_CHANGE
            it[content] = body.status.name
            it[createdAt] = now
        } get CommentsTable.id
        val updatedIncident = IncidentsTable.selectAll()
            .where { IncidentsTable.id eq incidentId }
            .first().toIncident()
        val auditComment = Comment(
            id = commentId.value,
            incidentId = incidentId,
            authorRole = AuthorRole.DISPATCHER,
            type = CommentType.STATUS_CHANGE,
            content = body.status.name,
            createdAt = now,
        )
        PatchResult(updatedIncident, auditComment)
    }
    if (result == null) {
        call.respond(HttpStatusCode.NotFound)
        return
    }
    call.respond(result.incident)
    broadcaster.emit(incidentUpdated(result.incident), result.incident.userId)
    broadcaster.emit(commentAdded(result.comment), result.incident.userId)
}

private suspend fun handleUploadPhoto(call: ApplicationCall, broadcaster: EventBroadcaster) {
    val (userId, role) = call.jwtClaims() ?: return call.respond(HttpStatusCode.Unauthorized)
    val incidentId = call.parameters["id"]?.toIntOrNull()
        ?: return call.respond(HttpStatusCode.BadRequest)
    val incident = transaction {
        IncidentsTable.selectAll().where { IncidentsTable.id eq incidentId }.firstOrNull()?.toIncident()
    } ?: return call.respond(HttpStatusCode.NotFound)
    if (role != Role.DISPATCHER.name && incident.userId != userId) {
        call.respond(HttpStatusCode.Forbidden)
        return
    }

    var photoBytes: ByteArray? = null
    var photoContentType: ContentType? = null
    call.receiveMultipart().forEachPart { part ->
        if (part is PartData.FileItem && part.name == "photo") {
            photoContentType = part.contentType
            photoBytes = part.provider().toByteArray()
        }
        part.release()  // must release every part or the multipart channel won't drain
    }

    val bytes = photoBytes ?: run {
        call.respondText("Missing photo field", status = HttpStatusCode.BadRequest)
        return
    }
    val allowed = setOf(ContentType.Image.JPEG, ContentType.Image.PNG)
    if (photoContentType !in allowed) {
        call.respond(HttpStatusCode.UnsupportedMediaType)
        return
    }
    if (bytes.size > 5 * 1024 * 1024) {  // 5 MB cap
        call.respond(HttpStatusCode.PayloadTooLarge)
        return
    }

    val ext = if (photoContentType == ContentType.Image.JPEG) "jpg" else "png"
    val filename = "$incidentId-${java.util.UUID.randomUUID()}.$ext"  // UUID avoids collisions on re-upload
    val uploadsDir = java.io.File("uploads")
    uploadsDir.resolve(filename).writeBytes(bytes)

    val photoUrl = "/uploads/$filename"
    val now = java.time.Instant.now().toString()
    val updated = transaction {
        IncidentsTable.update({ IncidentsTable.id eq incidentId }) {
            it[IncidentsTable.photoUrl] = photoUrl
            it[updatedAt] = now
        }
        IncidentsTable.selectAll().where { IncidentsTable.id eq incidentId }.first().toIncident()
    }
    call.respond(updated)
    broadcaster.emit(incidentUpdated(updated), updated.userId)
}

private suspend fun handleListComments(call: ApplicationCall) {
    val (userId, role) = call.jwtClaims() ?: return call.respond(HttpStatusCode.Unauthorized)
    val incidentId = call.parameters["id"]?.toIntOrNull()
        ?: return call.respond(HttpStatusCode.BadRequest)
    val incident = transaction {
        IncidentsTable.selectAll().where { IncidentsTable.id eq incidentId }.firstOrNull()?.toIncident()
    } ?: return call.respond(HttpStatusCode.NotFound)
    if (role != Role.DISPATCHER.name && incident.userId != userId) {
        call.respond(HttpStatusCode.Forbidden)
        return
    }
    val comments = transaction {
        CommentsTable.selectAll()
            .where { CommentsTable.incidentId eq EntityID(incidentId, IncidentsTable) }
            .orderBy(CommentsTable.createdAt to SortOrder.ASC)
            .map { it.toComment() }
    }
    call.respond(comments)
}

private suspend fun handlePostComment(call: ApplicationCall, broadcaster: EventBroadcaster) {
    val (userId, role) = call.jwtClaims() ?: return call.respond(HttpStatusCode.Unauthorized)
    val incidentId = call.parameters["id"]?.toIntOrNull()
        ?: return call.respond(HttpStatusCode.BadRequest)
    val incident = transaction {
        IncidentsTable.selectAll().where { IncidentsTable.id eq incidentId }.firstOrNull()?.toIncident()
    } ?: return call.respond(HttpStatusCode.NotFound)
    if (role != Role.DISPATCHER.name && incident.userId != userId) {
        call.respond(HttpStatusCode.Forbidden)
        return
    }
    val body = call.receive<PostCommentRequest>()
    if (body.content.isBlank()) {
        call.respondText("Content cannot be blank", status = HttpStatusCode.BadRequest)
        return
    }
    val now = java.time.Instant.now().toString()
    val authorRole = if (role == Role.DISPATCHER.name) AuthorRole.DISPATCHER else AuthorRole.ROAD_USER
    val comment = transaction {
        val id = CommentsTable.insert {
            it[CommentsTable.incidentId] = EntityID(incidentId, IncidentsTable)
            it[CommentsTable.authorRole] = authorRole
            it[CommentsTable.type] = CommentType.MESSAGE
            it[content] = body.content
            it[createdAt] = now
        } get CommentsTable.id
        Comment(
            id = id.value,
            incidentId = incidentId,
            authorRole = authorRole,
            type = CommentType.MESSAGE,
            content = body.content,
            createdAt = now,
        )
    }
    call.respond(HttpStatusCode.Created, comment)
    broadcaster.emit(commentAdded(comment), incident.userId)
}

private fun ResultRow.toComment(): Comment = Comment(
    id = this[CommentsTable.id].value,
    incidentId = this[CommentsTable.incidentId].value,
    authorRole = this[CommentsTable.authorRole],
    type = this[CommentsTable.type],
    content = this[CommentsTable.content],
    createdAt = this[CommentsTable.createdAt],
)

private fun ResultRow.toIncident(): Incident = Incident(
    id = this[IncidentsTable.id].value,
    userId = this[IncidentsTable.userId].value,
    category = this[IncidentsTable.category],
    description = this[IncidentsTable.description],
    latitude = this[IncidentsTable.latitude],
    longitude = this[IncidentsTable.longitude],
    photoUrl = this[IncidentsTable.photoUrl],
    status = this[IncidentsTable.status],
    notes = this[IncidentsTable.notes],
    createdAt = this[IncidentsTable.createdAt],
    updatedAt = this[IncidentsTable.updatedAt],
)
