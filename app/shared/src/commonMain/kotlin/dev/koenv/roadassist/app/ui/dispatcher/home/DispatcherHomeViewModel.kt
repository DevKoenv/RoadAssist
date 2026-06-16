package dev.koenv.roadassist.app.ui.dispatcher.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.koenv.roadassist.app.data.api.ApiClient
import dev.koenv.roadassist.app.data.incidents.IncidentRepository
import dev.koenv.roadassist.app.data.storage.SecureStorage
import dev.koenv.roadassist.core.auth.RefreshRequest
import dev.koenv.roadassist.core.incident.Incident
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DispatcherHomeViewModel(
    private val apiClient: ApiClient,
    private val storage: SecureStorage,
    private val repository: IncidentRepository,
) : ViewModel() {

    private val _serverReachable = MutableStateFlow(true)
    val serverReachable: StateFlow<Boolean> = _serverReachable.asStateFlow()

    val incidents: StateFlow<List<Incident>> = repository.observeIncidents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _incidentsLoading = MutableStateFlow(false)
    val incidentsLoading: StateFlow<Boolean> = _incidentsLoading.asStateFlow()

    init {
        viewModelScope.launch {
            while (true) {
                _serverReachable.value = apiClient.checkConnectivity()
                delay(10_000L)
            }
        }
        viewModelScope.launch {
            serverReachable.filter { it }.collect { syncIncidents() }
        }
        viewModelScope.launch {
            while (true) {
                delay(15_000L)
                syncIncidents()
            }
        }
    }

    fun refreshIncidents() {
        viewModelScope.launch { syncIncidents() }
    }

    private suspend fun syncIncidents() {
        _incidentsLoading.value = true
        repository.syncIncidents()
        _incidentsLoading.value = false
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
