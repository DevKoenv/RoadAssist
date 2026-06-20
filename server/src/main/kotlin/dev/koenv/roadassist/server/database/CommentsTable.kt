package dev.koenv.roadassist.server.database

import dev.koenv.roadassist.core.comment.AuthorRole
import dev.koenv.roadassist.core.comment.CommentType
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

object CommentsTable : IntIdTable("comments") {
    val incidentId = reference("incident_id", IncidentsTable)
    val authorRole = enumerationByName<AuthorRole>("author_role", 20)
    val type = enumerationByName<CommentType>("type", 20)
    val content = text("content")
    val createdAt = varchar("created_at", 50)
}
