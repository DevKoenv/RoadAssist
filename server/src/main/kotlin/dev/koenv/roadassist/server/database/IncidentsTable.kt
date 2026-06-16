package dev.koenv.roadassist.server.database

import dev.koenv.roadassist.core.incident.IncidentCategory
import dev.koenv.roadassist.core.incident.IncidentStatus
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

object IncidentsTable : IntIdTable("incidents") {
    val userId = reference("user_id", UsersTable)
    val category = enumerationByName<IncidentCategory>("category", 50)
    val description = text("description")
    val latitude = double("latitude")
    val longitude = double("longitude")
    val photoUrl = varchar("photo_url", 500).nullable()
    val status = enumerationByName<IncidentStatus>("status", 50)
    val notes = text("notes").nullable()
    val createdAt = varchar("created_at", 50)
    val updatedAt = varchar("updated_at", 50)
}
