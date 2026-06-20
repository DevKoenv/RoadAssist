package dev.koenv.roadassist.server.events

import dev.koenv.roadassist.core.comment.Comment
import dev.koenv.roadassist.core.incident.Incident
import io.ktor.sse.ServerSentEvent
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val json = Json { encodeDefaults = true }

fun incidentCreated(incident: Incident): ServerSentEvent =
    ServerSentEvent(event = "INCIDENT_CREATED", data = json.encodeToString(incident))

fun incidentUpdated(incident: Incident): ServerSentEvent =
    ServerSentEvent(event = "INCIDENT_UPDATED", data = json.encodeToString(incident))

fun commentAdded(comment: Comment): ServerSentEvent =
    ServerSentEvent(event = "COMMENT_ADDED", data = json.encodeToString(comment))
