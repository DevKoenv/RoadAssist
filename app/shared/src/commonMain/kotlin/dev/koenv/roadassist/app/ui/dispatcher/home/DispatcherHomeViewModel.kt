package dev.koenv.roadassist.app.ui.dispatcher.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.koenv.roadassist.app.data.api.ApiClient
import dev.koenv.roadassist.app.data.incidents.IncidentRepository
import dev.koenv.roadassist.app.data.sse.EventStreamService
import dev.koenv.roadassist.app.data.storage.SecureStorage
import dev.koenv.roadassist.core.auth.RefreshRequest
import dev.koenv.roadassist.core.incident.Incident
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DispatcherHomeViewModel(
    private val apiClient: ApiClient,
    private val storage: SecureStorage,
    private val repository: IncidentRepository,
    private val eventStreamService: EventStreamService,
) : ViewModel() {

    val serverReachable: StateFlow<Boolean> = eventStreamService.connectionState
        .map { it is EventStreamService.ConnectionState.Connected }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val incidents: StateFlow<List<Incident>> = repository.observeIncidents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _incidentsLoading = MutableStateFlow(false)
    val incidentsLoading: StateFlow<Boolean> = _incidentsLoading.asStateFlow()

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
