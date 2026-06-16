package dev.koenv.roadassist.app

import androidx.lifecycle.viewModelScope
import dev.koenv.roadassist.app.data.api.ApiException
import dev.koenv.roadassist.app.data.storage.SecureStorage
import dev.koenv.roadassist.app.data.storage.createSecureStorage
import dev.koenv.roadassist.app.ui.auth.login.LoginState
import dev.koenv.roadassist.app.ui.auth.login.LoginViewModel
import dev.koenv.roadassist.core.user.Role
import dev.koenv.roadassist.core.auth.AuthResponse
import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private var tempDir: File? = null
    private lateinit var storage: SecureStorage
    private var vm: LoginViewModel? = null

    @BeforeTest
    fun setup() {
        tempDir = Files.createTempDirectory("roadassist-vm-test").toFile()
        System.setProperty("roadassist.storageDir", tempDir!!.absolutePath)
        storage = createSecureStorage()
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun cleanup() {
        // Cancel the ViewModel's infinite polling loop before resetting the dispatcher,
        // otherwise the coroutine lingers across tests and the JVM never exits.
        vm?.viewModelScope?.cancel()
        vm = null
        Dispatchers.resetMain()
        System.clearProperty("roadassist.storageDir")
        tempDir?.deleteRecursively()
    }

    @Test
    fun initial_state_is_idle() {
        vm = LoginViewModel(FakeApiClient(Result.failure(ApiException.Timeout())), storage)
        assertIs<LoginState.Idle>(vm!!.state.value)
    }

    @Test
    fun login_success_emits_success_state_and_saves_tokens() = runTest {
        val response = AuthResponse(token = "access-tok", refreshToken = "refresh-tok", role = Role.ROAD_USER)
        vm = LoginViewModel(FakeApiClient(Result.success(response)), storage)
        try {
            vm!!.login("alice", "secret")

            val state = vm!!.state.value
            assertIs<LoginState.Success>(state)
            assertEquals(Role.ROAD_USER, state.role)
            assertEquals("access-tok", storage.getToken())
            assertEquals("refresh-tok", storage.getRefreshToken())
        } finally {
            // Cancel the ViewModel's infinite polling loop before runTest's advanceUntilIdle()
            // runs. runTest shares Dispatchers.Main's TestCoroutineScheduler, so advanceUntilIdle()
            // would otherwise keep advancing virtual time through the while(true) loop forever.
            vm!!.viewModelScope.cancel()
        }
    }

    @Test
    fun login_401_emits_invalid_credentials_error() = runTest {
        vm = LoginViewModel(FakeApiClient(Result.failure(ApiException.Unauthorized())), storage)
        try {
            vm!!.login("alice", "wrong")

            val state = vm!!.state.value
            assertIs<LoginState.Error>(state)
            assertEquals("Invalid credentials", state.message)
        } finally {
            vm!!.viewModelScope.cancel()
        }
    }

    @Test
    fun login_timeout_emits_server_unreachable_error() = runTest {
        vm = LoginViewModel(FakeApiClient(Result.failure(ApiException.Timeout())), storage)
        try {
            vm!!.login("alice", "secret")

            val state = vm!!.state.value
            assertIs<LoginState.Error>(state)
            assertEquals("Could not reach the server", state.message)
        } finally {
            vm!!.viewModelScope.cancel()
        }
    }
}
