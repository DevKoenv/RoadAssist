package dev.koenv.roadassist.app.data.api

import dev.koenv.roadassist.app.data.auth.AuthEventBus
import dev.koenv.roadassist.app.data.storage.SecureStorage
import dev.koenv.roadassist.app.network.createHttpClient
import dev.koenv.roadassist.core.Comment
import dev.koenv.roadassist.core.CreateIncidentRequest
import dev.koenv.roadassist.core.Incident
import dev.koenv.roadassist.core.PatchIncidentStatusRequest
import dev.koenv.roadassist.core.PostCommentRequest
import dev.koenv.roadassist.core.auth.AuthResponse
import dev.koenv.roadassist.core.auth.LoginRequest
import dev.koenv.roadassist.core.auth.RefreshRequest
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.plugin
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.AttributeKey
import kotlinx.coroutines.withTimeoutOrNull

// Prevents re-entering the refresh flow on the retry request itself
private val retryAfterRefreshKey = AttributeKey<Boolean>("RetryAfterRefresh")
private const val TIMEOUT_MS = 10_000L
private val publicPaths = setOf("/auth/login", "/auth/register", "/auth/refresh", "/health")

class KtorApiClient(private val storage: SecureStorage) : ApiClient {

    private val httpClient = createHttpClient().config {
        install(ContentNegotiation) { json() }
        install(HttpTimeout) {
            requestTimeoutMillis = TIMEOUT_MS
            connectTimeoutMillis = TIMEOUT_MS
            socketTimeoutMillis = TIMEOUT_MS
        }
    }

    init {
        // Intercept every request: inject the access token, then silently refresh on 401
        httpClient.plugin(HttpSend).intercept { requestBuilder ->
            val urlString = requestBuilder.url.buildString()
            val isPublic = publicPaths.any { urlString.contains(it) }
            val isRetry = requestBuilder.attributes.getOrNull(retryAfterRefreshKey) ?: false

            if (!isPublic) {
                storage.getToken()?.let { token ->
                    requestBuilder.headers {
                        set(HttpHeaders.Authorization, "Bearer $token")
                    }
                }
            }

            val call = execute(requestBuilder)

            if (call.response.status == HttpStatusCode.Unauthorized && !isPublic && !isRetry) {
                val refreshToken = storage.getRefreshToken()
                if (refreshToken != null) {
                    val refreshResponse = runCatching {
                        httpClient.post("$BASE_URL/auth/refresh") {
                            contentType(ContentType.Application.Json)
                            setBody(RefreshRequest(refreshToken))
                        }
                    }
                    val refreshBody = refreshResponse.getOrNull()
                    if (refreshBody != null && refreshBody.status.isSuccess()) {
                        val authResp = refreshBody.body<AuthResponse>()
                        storage.saveToken(authResp.token)
                        storage.saveRefreshToken(authResp.refreshToken)
                        requestBuilder.headers {
                            set(HttpHeaders.Authorization, "Bearer ${authResp.token}")
                        }
                        requestBuilder.attributes.put(retryAfterRefreshKey, true)
                        execute(requestBuilder)
                    } else {
                        // Refresh failed; clear tokens and kick back to login
                        storage.clearToken()
                        storage.clearRefreshToken()
                        AuthEventBus.notifyUnauthorized()
                        call
                    }
                } else {
                    // No refresh token stored; nothing to try
                    AuthEventBus.notifyUnauthorized()
                    call
                }
            } else {
                call
            }
        }
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    override suspend fun login(request: LoginRequest): Result<AuthResponse> = try {
        val response = httpClient.post("$BASE_URL/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        when {
            response.status.isSuccess() -> Result.success(response.body())
            response.status == HttpStatusCode.Unauthorized -> Result.failure(ApiException.Unauthorized())
            else -> Result.failure(ApiException.Network(RuntimeException("HTTP ${response.status.value}")))
        }
    } catch (e: HttpRequestTimeoutException) {
        Result.failure(ApiException.Timeout())
    } catch (e: Exception) {
        Result.failure(ApiException.Network(e))
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    override suspend fun refresh(request: RefreshRequest): Result<AuthResponse> = try {
        val response = httpClient.post("$BASE_URL/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        when {
            response.status.isSuccess() -> Result.success(response.body())
            response.status == HttpStatusCode.Unauthorized -> Result.failure(ApiException.Unauthorized())
            else -> Result.failure(ApiException.Network(RuntimeException("HTTP ${response.status.value}")))
        }
    } catch (e: HttpRequestTimeoutException) {
        Result.failure(ApiException.Timeout())
    } catch (e: Exception) {
        Result.failure(ApiException.Network(e))
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    override suspend fun checkConnectivity(): Boolean = try {
        withTimeoutOrNull(3_000L) {
            httpClient.get("$BASE_URL/health").status.isSuccess()
        } ?: false
    } catch (e: Exception) {
        false
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    override suspend fun logout(request: RefreshRequest) {
        try {
            httpClient.post("$BASE_URL/auth/logout") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        } catch (e: Exception) {
            // fire and forget: logout failure is non-blocking
        }
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    override suspend fun createIncident(request: CreateIncidentRequest): Result<Incident> = try {
        val response = httpClient.post("$BASE_URL/incidents") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        when {
            response.status.isSuccess() -> Result.success(response.body<Incident>().withAbsolutePhotoUrl())
            response.status == HttpStatusCode.Unauthorized -> Result.failure(ApiException.Unauthorized())
            else -> Result.failure(ApiException.Network(RuntimeException("HTTP ${response.status.value}")))
        }
    } catch (e: HttpRequestTimeoutException) {
        Result.failure(ApiException.Timeout())
    } catch (e: Exception) {
        Result.failure(ApiException.Network(e))
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    override suspend fun getIncidents(): Result<List<Incident>> = try {
        val response = httpClient.get("$BASE_URL/incidents")
        when {
            response.status.isSuccess() -> Result.success(response.body<List<Incident>>().map { it.withAbsolutePhotoUrl() })
            response.status == HttpStatusCode.Unauthorized -> Result.failure(ApiException.Unauthorized())
            else -> Result.failure(ApiException.Network(RuntimeException("HTTP ${response.status.value}")))
        }
    } catch (e: HttpRequestTimeoutException) {
        Result.failure(ApiException.Timeout())
    } catch (e: Exception) {
        Result.failure(ApiException.Network(e))
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    override suspend fun getIncident(id: Int): Result<Incident> = try {
        val response = httpClient.get("$BASE_URL/incidents/$id")
        when {
            response.status.isSuccess() -> Result.success(response.body<Incident>().withAbsolutePhotoUrl())
            response.status == HttpStatusCode.Unauthorized -> Result.failure(ApiException.Unauthorized())
            else -> Result.failure(ApiException.Network(RuntimeException("HTTP ${response.status.value}")))
        }
    } catch (e: HttpRequestTimeoutException) {
        Result.failure(ApiException.Timeout())
    } catch (e: Exception) {
        Result.failure(ApiException.Network(e))
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    override suspend fun patchIncidentStatus(id: Int, request: PatchIncidentStatusRequest): Result<Incident> = try {
        val response = httpClient.patch("$BASE_URL/incidents/$id/status") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        when {
            response.status.isSuccess() -> Result.success(response.body<Incident>().withAbsolutePhotoUrl())
            response.status == HttpStatusCode.Unauthorized -> Result.failure(ApiException.Unauthorized())
            else -> Result.failure(ApiException.Network(RuntimeException("HTTP ${response.status.value}")))
        }
    } catch (e: HttpRequestTimeoutException) {
        Result.failure(ApiException.Timeout())
    } catch (e: Exception) {
        Result.failure(ApiException.Network(e))
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    override suspend fun getComments(incidentId: Int): Result<List<Comment>> = try {
        val response = httpClient.get("$BASE_URL/incidents/$incidentId/comments")
        when {
            response.status.isSuccess() -> Result.success(response.body())
            response.status == HttpStatusCode.Unauthorized -> Result.failure(ApiException.Unauthorized())
            else -> Result.failure(ApiException.Network(RuntimeException("HTTP ${response.status.value}")))
        }
    } catch (e: HttpRequestTimeoutException) {
        Result.failure(ApiException.Timeout())
    } catch (e: Exception) {
        Result.failure(ApiException.Network(e))
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    override suspend fun postComment(incidentId: Int, content: String): Result<Comment> = try {
        val response = httpClient.post("$BASE_URL/incidents/$incidentId/comments") {
            contentType(ContentType.Application.Json)
            setBody(PostCommentRequest(content))
        }
        when {
            response.status.isSuccess() -> Result.success(response.body())
            response.status == HttpStatusCode.Unauthorized -> Result.failure(ApiException.Unauthorized())
            else -> Result.failure(ApiException.Network(RuntimeException("HTTP ${response.status.value}")))
        }
    } catch (e: HttpRequestTimeoutException) {
        Result.failure(ApiException.Timeout())
    } catch (e: Exception) {
        Result.failure(ApiException.Network(e))
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    override suspend fun uploadPhoto(incidentId: Int, imageBytes: ByteArray, mimeType: String): Result<Incident> = try {
        val ext = if (mimeType == "image/png") "png" else "jpg"
        val response = httpClient.post("$BASE_URL/incidents/$incidentId/photo") {
            setBody(
                MultiPartFormDataContent(formData {
                    append("photo", imageBytes, Headers.build {
                        append(io.ktor.http.HttpHeaders.ContentType, mimeType)
                        append(
                            io.ktor.http.HttpHeaders.ContentDisposition,
                            "form-data; name=\"photo\"; filename=\"photo.$ext\"",
                        )
                    })
                })
            )
        }
        when {
            response.status.isSuccess() -> Result.success(response.body<Incident>().withAbsolutePhotoUrl())
            response.status == HttpStatusCode.Unauthorized -> Result.failure(ApiException.Unauthorized())
            else -> Result.failure(ApiException.Network(RuntimeException("HTTP ${response.status.value}")))
        }
    } catch (e: HttpRequestTimeoutException) {
        Result.failure(ApiException.Timeout())
    } catch (e: Exception) {
        Result.failure(ApiException.Network(e))
    }

    private fun Incident.withAbsolutePhotoUrl(): Incident =
        if (photoUrl?.startsWith("/") == true) copy(photoUrl = "$BASE_URL$photoUrl") else this
}
