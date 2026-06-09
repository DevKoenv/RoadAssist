package dev.koenv.roadassist.app

interface SecureStorage {
    fun saveToken(token: String)
    fun getToken(): String?
    fun clearToken()
    fun saveRefreshToken(token: String)
    fun getRefreshToken(): String?
    fun clearRefreshToken()
}

expect fun createSecureStorage(): SecureStorage
