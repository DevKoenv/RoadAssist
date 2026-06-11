package dev.koenv.roadassist.server

import dev.koenv.roadassist.core.CreateIncidentRequest
import dev.koenv.roadassist.core.Incident
import dev.koenv.roadassist.core.IncidentStatus
import dev.koenv.roadassist.core.Role
import dev.koenv.roadassist.server.database.IncidentsTable
import dev.koenv.roadassist.server.database.UsersTable
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
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

fun Route.configureIncidentsRouting() {
    route("/incidents") {
        post { handleCreateIncident(call) }
        get { handleListIncidents(call) }
        route("/{id}") {
            get { handleGetIncident(call) }
            patch("/status") { handlePatchStatus(call) }
            post("/photo") { handleUploadPhoto(call) }
        }
    }
}

private suspend fun handleCreateIncident(call: ApplicationCall) {
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
}

private suspend fun handleListIncidents(call: ApplicationCall) {
    val (userId, role) = call.jwtClaims() ?: return call.respond(HttpStatusCode.Unauthorized)
    val incidents = transaction {
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

private suspend fun handlePatchStatus(call: ApplicationCall) {
    val (_, role) = call.jwtClaims() ?: return call.respond(HttpStatusCode.Unauthorized)
    if (role != Role.DISPATCHER.name) {
        call.respond(HttpStatusCode.Forbidden)
        return
    }
    val incidentId = call.parameters["id"]?.toIntOrNull()
        ?: return call.respond(HttpStatusCode.BadRequest)
    val body = runCatching {
        call.receive<dev.koenv.roadassist.core.PatchIncidentStatusRequest>()
    }.getOrElse {
        call.respondText("Invalid status value", status = HttpStatusCode.BadRequest)
        return
    }
    val now = java.time.Instant.now().toString()
    val updated = transaction {
        IncidentsTable.selectAll()
            .where { IncidentsTable.id eq incidentId }
            .firstOrNull() ?: return@transaction null
        IncidentsTable.update({ IncidentsTable.id eq incidentId }) {
            it[status] = body.status
            it[notes] = body.notes
            it[updatedAt] = now
        }
        IncidentsTable.selectAll()
            .where { IncidentsTable.id eq incidentId }
            .first().toIncident()
    }
    if (updated == null) {
        call.respond(HttpStatusCode.NotFound)
        return
    }
    call.respond(updated)
}

private suspend fun handleUploadPhoto(call: ApplicationCall) {
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
        part.release()
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
    if (bytes.size > 5 * 1024 * 1024) {
        call.respond(HttpStatusCode.PayloadTooLarge)
        return
    }

    val ext = if (photoContentType == ContentType.Image.JPEG) "jpg" else "png"
    val filename = "$incidentId-${java.util.UUID.randomUUID()}.$ext"
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
}

private fun ApplicationCall.jwtClaims(): Pair<Int, String>? {
    val principal = principal<JWTPrincipal>() ?: return null
    val userId = principal.payload.subject?.toIntOrNull() ?: return null
    val role = principal.payload.getClaim("role")?.asString() ?: return null
    return userId to role
}

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
