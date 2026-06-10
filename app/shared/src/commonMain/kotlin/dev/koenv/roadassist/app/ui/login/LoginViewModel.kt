package dev.koenv.roadassist.app.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.koenv.roadassist.app.data.api.ApiClient
import dev.koenv.roadassist.app.data.api.ApiException
import dev.koenv.roadassist.app.data.storage.SecureStorage
import dev.koenv.roadassist.core.LoginRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val apiClient: ApiClient,
    private val storage: SecureStorage,
) : ViewModel() {

    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state: StateFlow<LoginState> = _state.asStateFlow()

    private val _serverReachable = MutableStateFlow(true)
    val serverReachable: StateFlow<Boolean> = _serverReachable.asStateFlow()

    init {
        viewModelScope.launch {
            while (true) {
                _serverReachable.value = apiClient.checkConnectivity()
                delay(10_000L)
            }
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _state.value = LoginState.Loading
            apiClient.login(LoginRequest(username, password)).fold(
                onSuccess = { response ->
                    storage.saveToken(response.token)
                    storage.saveRefreshToken(response.refreshToken)
                    _state.value = LoginState.Success(response.role)
                },
                onFailure = { error ->
                    _state.value = when (error) {
                        is ApiException.Unauthorized -> LoginState.Error("Invalid credentials")
                        is ApiException.Timeout,
                        is ApiException.Network -> LoginState.Error("Could not reach the server")
                        else -> LoginState.Error("An unexpected error occurred")
                    }
                },
            )
        }
    }
}
