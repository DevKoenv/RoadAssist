package dev.koenv.roadassist.app.data.sse

import dev.koenv.roadassist.app.data.api.BASE_URL
import dev.koenv.roadassist.app.data.incidents.IncidentRepository
import dev.koenv.roadassist.app.data.storage.SecureStorage
import dev.koenv.roadassist.app.network.createHttpClient
import dev.koenv.roadassist.core.comment.Comment
import dev.koenv.roadassist.core.incident.Incident
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.HttpTimeoutConfig
import io.ktor.client.request.headers
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpHeaders
import io.ktor.utils.io.readUTF8Line
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class EventStreamService(
    private val storage: SecureStorage,
    private val repository: IncidentRepository,
) {
    sealed class ConnectionState {
        object Connecting : ConnectionState()
        object Connected : ConnectionState()
        object Reconnecting : ConnectionState()
        object Stopped : ConnectionState()
    }

    sealed class SseEvent {
        data class IncidentCreated(val incident: Incident) : SseEvent()
        data class IncidentUpdated(val incident: Incident) : SseEvent()
        data class CommentAdded(val comment: Comment) : SseEvent()
        data class Unknown(val type: String?) : SseEvent()
    }

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Stopped)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _reconnects = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val reconnects: Flow<Unit> = _reconnects.asSharedFlow()

    private val httpClient = createHttpClient().config {
        install(HttpTimeout) {
            requestTimeoutMillis = HttpTimeoutConfig.INFINITE_TIMEOUT_MS
            connectTimeoutMillis = 10_000L
            socketTimeoutMillis = HttpTimeoutConfig.INFINITE_TIMEOUT_MS
        }
    }

    private var serviceScope: CoroutineScope? = null

    fun start() {
        val token = storage.getToken() ?: return
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        serviceScope = scope
        scope.launch {
            connectWithRetry(token).collect { event -> handleEvent(event) }
        }
    }

    fun stop() {
        serviceScope?.cancel()
        serviceScope = null
        _connectionState.value = ConnectionState.Stopped
    }

    private fun connectWithRetry(token: String): Flow<SseEvent> {
        var wasReconnecting = false
        return flow {
            val collector = this
            if (wasReconnecting) {
                repository.syncIncidents()
                _reconnects.tryEmit(Unit)
                wasReconnecting = false
            }
            _connectionState.value = ConnectionState.Connecting
            httpClient.prepareGet("$BASE_URL/events") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $token")
                    append(HttpHeaders.Accept, "text/event-stream")
                    append(HttpHeaders.CacheControl, "no-cache")
                }
            }.execute { response ->
                _connectionState.value = ConnectionState.Connected
                val channel = response.bodyAsChannel()
                var currentEvent: String? = null
                val currentData = StringBuilder()
                while (!channel.isClosedForRead) {
                    val line = channel.readUTF8Line() ?: break
                    when {
                        line.startsWith("event:") -> currentEvent = line.removePrefix("event:").trim()
                        line.startsWith("data:") -> {
                            if (currentData.isNotEmpty()) currentData.append("\n")
                            currentData.append(line.removePrefix("data:").trim())
                        }
                        line.startsWith(":") -> { /* heartbeat/comment, ignore */ }
                        line.isEmpty() && currentData.isNotEmpty() -> {
                            collector.emit(parseEvent(currentEvent, currentData.toString()))
                            currentEvent = null
                            currentData.clear()
                        }
                    }
                }
            }
            throw IOException("SSE stream ended; reconnecting")
        }.retryWhen { cause, attempt ->
            if (cause is CancellationException) return@retryWhen false
            wasReconnecting = true
            _connectionState.value = ConnectionState.Reconnecting
            val delayMs = minOf(1000L * (1L shl attempt.toInt().coerceAtMost(5)), 60_000L)
            delay(delayMs)
            true
        }
    }

    private fun handleEvent(event: SseEvent) {
        when (event) {
            is SseEvent.IncidentCreated -> repository.upsertIncident(event.incident.withAbsolutePhotoUrl())
            is SseEvent.IncidentUpdated -> repository.upsertIncident(event.incident.withAbsolutePhotoUrl())
            is SseEvent.CommentAdded -> repository.upsertComment(event.comment)
            is SseEvent.Unknown -> Unit
        }
    }

    internal fun parseEvent(eventType: String?, data: String): SseEvent = when (eventType) {
        "INCIDENT_CREATED" -> runCatching { SseEvent.IncidentCreated(json.decodeFromString(data)) }.getOrElse { SseEvent.Unknown(eventType) }
        "INCIDENT_UPDATED" -> runCatching { SseEvent.IncidentUpdated(json.decodeFromString(data)) }.getOrElse { SseEvent.Unknown(eventType) }
        "COMMENT_ADDED" -> runCatching { SseEvent.CommentAdded(json.decodeFromString(data)) }.getOrElse { SseEvent.Unknown(eventType) }
        else -> SseEvent.Unknown(eventType)
    }

    private fun Incident.withAbsolutePhotoUrl(): Incident =
        if (photoUrl?.startsWith("/") == true) copy(photoUrl = "$BASE_URL$photoUrl") else this
}

private val json = Json { ignoreUnknownKeys = true }
