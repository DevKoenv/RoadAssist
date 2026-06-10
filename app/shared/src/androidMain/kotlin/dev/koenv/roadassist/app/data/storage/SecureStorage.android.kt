package dev.koenv.roadassist.app.data.storage

import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

actual fun createSecureStorage(): SecureStorage {
    val context = AndroidContextHolder.applicationContext
    val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "roadassist_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )
    return AndroidSecureStorage(prefs)
}

private class AndroidSecureStorage(private val prefs: SharedPreferences) : SecureStorage {
    override fun saveToken(token: String) = prefs.edit().putString("token", token).apply()
    override fun getToken(): String? = prefs.getString("token", null)
    override fun clearToken() = prefs.edit().remove("token").apply()
    override fun saveRefreshToken(token: String) = prefs.edit().putString("refresh_token", token).apply()
    override fun getRefreshToken(): String? = prefs.getString("refresh_token", null)
    override fun clearRefreshToken() = prefs.edit().remove("refresh_token").apply()
}
