package dev.koenv.roadassist.app.data.storage

/**
 * Persists JWT access and refresh tokens using platform-appropriate encryption.
 *
 * Android: backed by [EncryptedSharedPreferences] with a hardware-backed AES-256-GCM master key
 * managed by the Android Keystore. Keys are encrypted with AES-256-SIV (deterministic, required
 * for SharedPreferences key lookup); values are encrypted with AES-256-GCM (randomised nonce
 * per write so ciphertext equality does not reveal whether a value changed).
 *
 * Desktop (JVM): tokens are stored in an AES-256-GCM encrypted file under `~/.roadassist/`.
 * The 256-bit key is generated once and stored alongside the encrypted data. This prevents casual
 * token inspection but is not hardened against an attacker with full filesystem access.
 */
interface SecureStorage {
    fun saveToken(token: String)
    fun getToken(): String?
    fun clearToken()
    fun saveRefreshToken(token: String)
    fun getRefreshToken(): String?
    fun clearRefreshToken()
}

/**
 * Returns the platform-appropriate [SecureStorage] implementation.
 * Call once and retain the instance for the application lifetime.
 */
expect fun createSecureStorage(): SecureStorage
