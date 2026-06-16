package dev.koenv.roadassist.app.ui.dispatcher.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.koenv.roadassist.app.data.incidents.IncidentRepository
import dev.koenv.roadassist.app.geocoding.GeocodingService
import dev.koenv.roadassist.core.incident.Incident
import dev.koenv.roadassist.core.incident.IncidentStatus
import dev.koenv.roadassist.core.incident.PatchIncidentStatusRequest
import dev.koenv.roadassist.core.location.LatLon
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class UpdateState {
    object Idle : UpdateState()
    object Loading : UpdateState()
    data class Error(val message: String) : UpdateState()
}

class DispatcherDetailViewModel(
    private val repository: IncidentRepository,
    private val incidentId: Int,
    private val geocodingService: GeocodingService? = null,
) : ViewModel() {

    val incident: StateFlow<Incident?> = repository.observeIncident(incidentId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _selectedStatus = MutableStateFlow<IncidentStatus?>(null)
    val selectedStatus: StateFlow<IncidentStatus?> = _selectedStatus.asStateFlow()

    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes.asStateFlow()

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    val comments = repository.observeComments(incidentId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _address = MutableStateFlow<String?>(null)
    val address: StateFlow<String?> = _address.asStateFlow()

    private val _commentInput = MutableStateFlow("")
    val commentInput: StateFlow<String> = _commentInput.asStateFlow()

    private val _commentPosting = MutableStateFlow(false)
    val commentPosting: StateFlow<Boolean> = _commentPosting.asStateFlow()

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    private val _serverReachable = MutableStateFlow(true)
    val serverReachable: StateFlow<Boolean> = _serverReachable.asStateFlow()

    init {
        viewModelScope.launch {
            while (true) {
                _serverReachable.value = repository.checkConnectivity()
                delay(10_000L)
            }
        }
        viewModelScope.launch {
            var isFirstSync = true  // clear loading state once; subsequent connectivity events shouldn't re-show it
            serverReachable.filter { it }.collect {
                repository.syncIncident(incidentId)
                if (isFirstSync) {
                    _loading.value = false
                    isFirstSync = false
                }
            }
        }
        viewModelScope.launch {
            val inc = incident.filterNotNull().first()
            if (_selectedStatus.value == null) _selectedStatus.value = inc.status
            if (geocodingService != null) {
                _address.value = geocodingService.reverse(LatLon(inc.latitude, inc.longitude))
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _refreshing.value = true
            repository.syncIncident(incidentId)
            _refreshing.value = false
        }
    }

    fun selectStatus(status: IncidentStatus) { _selectedStatus.value = status }

    fun updateNotes(notes: String) { _notes.value = notes }

    fun updateCommentInput(text: String) { _commentInput.value = text }

    fun postComment() {
        if (_commentPosting.value) return
        val text = _commentInput.value.trim()
        if (text.isBlank()) return
        _commentPosting.value = true
        viewModelScope.launch {
            repository.postComment(incidentId, text)
                .onSuccess { _commentInput.value = "" }
            _commentPosting.value = false
        }
    }

    fun saveUpdate(onSuccess: () -> Unit) {
        if (_updateState.value is UpdateState.Loading) return
        val current = incident.value ?: return
        val status = _selectedStatus.value ?: return
        val message = _notes.value.trim()

        _updateState.value = UpdateState.Loading

        viewModelScope.launch {
            repository.patchIncidentStatus(current.id, PatchIncidentStatusRequest(status, null))
                .onSuccess {
                    if (message.isNotBlank()) {
                        repository.postComment(current.id, message).getOrNull()
                    }
                    _notes.value = ""
                    onSuccess()
                    _updateState.value = UpdateState.Idle
                }
                .onFailure {
                    _selectedStatus.value = current.status
                    _updateState.value = UpdateState.Error("Failed to update. Please try again.")
                }
        }
    }

    fun cancelEdit() {
        _selectedStatus.value = incident.value?.status
        _notes.value = ""
    }

    fun clearError() { _updateState.value = UpdateState.Idle }
}
