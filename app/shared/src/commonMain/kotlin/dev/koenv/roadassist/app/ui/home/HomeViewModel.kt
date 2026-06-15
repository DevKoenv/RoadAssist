package dev.koenv.roadassist.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.koenv.roadassist.app.data.api.ApiClient
import dev.koenv.roadassist.app.data.incidents.IncidentRepository
import dev.koenv.roadassist.app.data.storage.SecureStorage
import dev.koenv.roadassist.core.Incident
import dev.koenv.roadassist.core.RefreshRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val apiClient: ApiClient,
    private val storage: SecureStorage,
    private val repository: IncidentRepository,
) : ViewModel() {

    private val _serverReachable = MutableStateFlow(true)
    val serverReachable: StateFlow<Boolean> = _serverReachable.asStateFlow()

    private val _incidents = MutableStateFlow<List<Incident>>(emptyList())
    val incidents: StateFlow<List<Incident>> = _incidents.asStateFlow()

    private val _incidentsLoading = MutableStateFlow(false)
    val incidentsLoading: StateFlow<Boolean> = _incidentsLoading.asStateFlow()

    private val _selectedTab = MutableStateFlow(RoadUserTab.Active)
    val selectedTab: StateFlow<RoadUserTab> = _selectedTab.asStateFlow()

    init {
        _incidents.value = repository.loadCached()
        viewModelScope.launch {
            while (true) {
                _serverReachable.value = apiClient.checkConnectivity()
                delay(10_000L)
            }
        }
        viewModelScope.launch { loadIncidents() }
        viewModelScope.launch {
            while (true) {
                delay(15_000L)
                loadIncidents()
            }
        }
    }

    fun refreshIncidents() {
        viewModelScope.launch { loadIncidents() }
    }

    fun selectTab(tab: RoadUserTab) {
        _selectedTab.value = tab
    }

    private suspend fun loadIncidents() {
        _incidentsLoading.value = true
        repository.getIncidents().onSuccess { _incidents.value = it }
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
