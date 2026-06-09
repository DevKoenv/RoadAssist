package dev.koenv.roadassist.app

sealed class ApiException(message: String) : Exception(message) {
    class Unauthorized : ApiException("Unauthorized")
    class Timeout : ApiException("Request timed out")
    class Network(cause: Exception) : ApiException("Network error: ${cause.message}")
}
