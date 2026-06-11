package dev.koenv.roadassist.app

import dev.koenv.roadassist.app.data.storage.createSecureStorage
import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SecureStorageTest {

    private var tempDir: File? = null

    @BeforeTest
    fun setup() {
        tempDir = Files.createTempDirectory("roadassist-test").toFile()
        System.setProperty("roadassist.storageDir", tempDir!!.absolutePath)
    }

    @AfterTest
    fun cleanup() {
        System.clearProperty("roadassist.storageDir")
        tempDir?.deleteRecursively()
    }

    @Test
    fun token_is_null_before_save() {
        val storage = createSecureStorage()
        assertNull(storage.getToken())
    }

    @Test
    fun save_and_get_token() {
        val storage = createSecureStorage()
        storage.saveToken("access-token-123")
        assertEquals("access-token-123", storage.getToken())
    }

    @Test
    fun clear_token_returns_null() {
        val storage = createSecureStorage()
        storage.saveToken("access-token-123")
        storage.clearToken()
        assertNull(storage.getToken())
    }

    @Test
    fun refresh_token_is_null_before_save() {
        val storage = createSecureStorage()
        assertNull(storage.getRefreshToken())
    }

    @Test
    fun save_and_get_refresh_token() {
        val storage = createSecureStorage()
        storage.saveRefreshToken("refresh-token-456")
        assertEquals("refresh-token-456", storage.getRefreshToken())
    }

    @Test
    fun clear_refresh_token_returns_null() {
        val storage = createSecureStorage()
        storage.saveRefreshToken("refresh-token-456")
        storage.clearRefreshToken()
        assertNull(storage.getRefreshToken())
    }

    @Test
    fun token_and_refresh_token_are_independent() {
        val storage = createSecureStorage()
        storage.saveToken("tok-a")
        storage.saveRefreshToken("rtok-b")
        storage.clearToken()
        assertNull(storage.getToken())
        assertEquals("rtok-b", storage.getRefreshToken())
    }
}
