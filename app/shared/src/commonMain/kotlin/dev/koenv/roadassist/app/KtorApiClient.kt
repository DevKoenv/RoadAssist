package dev.koenv.roadassist.app

import dev.koenv.roadassist.core.AuthResponse
import dev.koenv.roadassist.core.LoginRequest
import dev.koenv.roadassist.core.RefreshRequest
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.plugin
import io.ktor.client.request.headers
import io.ktor.client.request.get
import io.ktor.client.request.post
import kotlinx.coroutines.withTimeoutOrNull
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.AttributeKey

private val retryAfterRefreshKey = AttributeKey<Boolean>("RetryAfterRefresh")
private const val TIMEOUT_MS = 10_000L
private val publicPaths = setOf("/auth/login", "/auth/register", "/auth/refresh")

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
                        storage.clearToken()
                        storage.clearRefreshToken()
                        AuthEventBus.notifyUnauthorized()
                        call
                    }
                } else {
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
    } catch (e: io.ktor.client.plugins.HttpRequestTimeoutException) {
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
    } catch (e: io.ktor.client.plugins.HttpRequestTimeoutException) {
        Result.failure(ApiException.Timeout())
    } catch (e: Exception) {
        Result.failure(ApiException.Network(e))
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    override suspend fun checkConnectivity(): Boolean = try {
        withTimeoutOrNull(3_000L) {
            httpClient.get("$BASE_URL/auth/login")
            true
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
}
