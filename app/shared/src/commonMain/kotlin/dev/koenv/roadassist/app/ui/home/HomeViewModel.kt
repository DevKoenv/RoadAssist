package dev.koenv.roadassist.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.koenv.roadassist.app.data.api.ApiClient
import dev.koenv.roadassist.app.data.storage.SecureStorage
import dev.koenv.roadassist.core.RefreshRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val apiClient: ApiClient,
    private val storage: SecureStorage,
) : ViewModel() {

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

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            val refreshToken = storage.getRefreshToken()
            if (refreshToken != null) {
                apiClient.logout(RefreshRequest(refreshToken))
            }
            storage.clearToken()
            storage.clearRefreshToken()
            onComplete()
        }
    }
}
