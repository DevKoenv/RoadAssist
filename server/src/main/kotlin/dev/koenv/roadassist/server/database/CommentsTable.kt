package dev.koenv.roadassist.server.database

import dev.koenv.roadassist.core.AuthorRole
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

object CommentsTable : IntIdTable("comments") {
    val incidentId = reference("incident_id", IncidentsTable)
    val authorRole = enumerationByName<AuthorRole>("author_role", 20)
    val content = text("content")
    val createdAt = varchar("created_at", 50)
}
