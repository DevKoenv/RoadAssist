package dev.koenv.roadassist.app.data.db

import dev.koenv.roadassist.app.db.CommentEntity
import dev.koenv.roadassist.app.db.CommentEntityQueries
import dev.koenv.roadassist.app.db.IncidentEntity
import dev.koenv.roadassist.app.db.IncidentEntityQueries
import dev.koenv.roadassist.core.AuthorRole
import dev.koenv.roadassist.core.Comment
import dev.koenv.roadassist.core.CommentType
import dev.koenv.roadassist.core.incident.Incident
import dev.koenv.roadassist.core.incident.IncidentCategory
import dev.koenv.roadassist.core.incident.IncidentStatus

fun IncidentEntity.toDomain(): Incident = Incident(
    id = id.toInt(),
    userId = userId.toInt(),
    category = runCatching { IncidentCategory.valueOf(category) }.getOrElse { IncidentCategory.OTHER },
    description = description,
    latitude = latitude,
    longitude = longitude,
    photoUrl = photoUrl,
    status = runCatching { IncidentStatus.valueOf(status) }.getOrElse { IncidentStatus.entries.first() },
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun IncidentEntityQueries.upsert(incident: Incident) = upsert(
    id = incident.id.toLong(),
    userId = incident.userId.toLong(),
    category = incident.category.name,
    description = incident.description,
    latitude = incident.latitude,
    longitude = incident.longitude,
    photoUrl = incident.photoUrl,
    status = incident.status.name,
    notes = incident.notes,
    createdAt = incident.createdAt,
    updatedAt = incident.updatedAt,
)

fun CommentEntity.toDomain(): Comment = Comment(
    id = id.toInt(),
    incidentId = incidentId.toInt(),
    authorRole = runCatching { AuthorRole.valueOf(authorRole) }.getOrElse { AuthorRole.entries.first() },
    type = runCatching { CommentType.valueOf(type) }.getOrElse { CommentType.entries.first() },
    content = content,
    createdAt = createdAt,
)

fun CommentEntityQueries.upsert(comment: Comment) = upsert(
    id = comment.id.toLong(),
    incidentId = comment.incidentId.toLong(),
    authorRole = comment.authorRole.name,
    type = comment.type.name,
    content = comment.content,
    createdAt = comment.createdAt,
)
