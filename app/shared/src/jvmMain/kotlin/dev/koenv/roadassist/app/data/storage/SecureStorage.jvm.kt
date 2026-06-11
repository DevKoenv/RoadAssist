package dev.koenv.roadassist.app.data.storage

import java.nio.file.Path
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes

actual fun createSecureStorage(): SecureStorage {
    val dir = System.getProperty("roadassist.storageDir")
        ?.let { Path.of(it) }
        ?: Path.of(System.getProperty("user.home"), ".roadassist")
    return JvmSecureStorage(dir)
}

class JvmSecureStorage(private val storageDir: Path) : SecureStorage {

    private val keyFile = storageDir.resolve("machine.key")
    private val dataFile = storageDir.resolve("tokens.enc")

    private val key: SecretKey by lazy {
        storageDir.createDirectories()
        if (keyFile.exists()) {
            SecretKeySpec(keyFile.readBytes(), "AES")
        } else {
            val generated = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()
            keyFile.writeBytes(generated.encoded)
            generated
        }
    }

    private fun encrypt(plaintext: String): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        val encrypted = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        return iv + encrypted
    }

    private fun decrypt(data: ByteArray): String {
        val iv = data.sliceArray(0 until 12)
        val ciphertext = data.sliceArray(12 until data.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
        return cipher.doFinal(ciphertext).toString(Charsets.UTF_8)
    }

    @Suppress("TooGenericExceptionCaught")
    private fun readAll(): Map<String, String> {
        if (!dataFile.exists()) return emptyMap()
        return runCatching {
            decrypt(dataFile.readBytes())
                .lines()
                .filter { it.contains('=') }
                .associate { line ->
                    val idx = line.indexOf('=')
                    line.substring(0, idx) to line.substring(idx + 1)
                }
        }.getOrElse { emptyMap() }
    }

    private fun writeAll(map: Map<String, String>) {
        storageDir.createDirectories()
        val plaintext = map.entries.joinToString("\n") { "${it.key}=${it.value}" }
        dataFile.writeBytes(encrypt(plaintext))
    }

    override fun saveToken(token: String) {
        writeAll(readAll() + ("token" to token))
    }

    override fun getToken(): String? = readAll()["token"]

    override fun clearToken() {
        writeAll(readAll() - "token")
    }

    override fun saveRefreshToken(token: String) {
        writeAll(readAll() + ("refresh_token" to token))
    }

    override fun getRefreshToken(): String? = readAll()["refresh_token"]

    override fun clearRefreshToken() {
        writeAll(readAll() - "refresh_token")
    }
}
