package dev.koenv.roadassist.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.koenv.roadassist.app.data.incidents.IncidentRepository
import dev.koenv.roadassist.app.geocoding.GeocodingService
import dev.koenv.roadassist.core.Comment
import dev.koenv.roadassist.core.Incident
import dev.koenv.roadassist.core.IncidentStatus
import dev.koenv.roadassist.core.LatLon
import dev.koenv.roadassist.core.PatchIncidentStatusRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _incident = MutableStateFlow<Incident?>(null)
    val incident: StateFlow<Incident?> = _incident.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _selectedStatus = MutableStateFlow<IncidentStatus?>(null)
    val selectedStatus: StateFlow<IncidentStatus?> = _selectedStatus.asStateFlow()

    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes.asStateFlow()

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments.asStateFlow()

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
            val result = repository.getIncident(incidentId).getOrNull()
            _incident.value = result
            _selectedStatus.value = result?.status
            _loading.value = false
            _comments.value = repository.getComments(incidentId).getOrElse { emptyList() }
            if (result != null && geocodingService != null) {
                _address.value = geocodingService.reverse(LatLon(result.latitude, result.longitude))
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _refreshing.value = true
            val result = repository.getIncident(incidentId).getOrNull()
            _incident.value = result
            _selectedStatus.value = result?.status ?: _selectedStatus.value
            _comments.value = repository.getComments(incidentId).getOrElse { _comments.value }
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
                .onSuccess { comment ->
                    _comments.value = _comments.value + comment
                    _commentInput.value = ""
                }
            _commentPosting.value = false
        }
    }

    fun saveUpdate(onSuccess: () -> Unit) {
        if (_updateState.value is UpdateState.Loading) return
        val current = _incident.value ?: return
        val status = _selectedStatus.value ?: return
        val message = _notes.value.trim()

        val previousIncident = current
        _updateState.value = UpdateState.Loading

        viewModelScope.launch {
            _incident.value = current.copy(status = status)

            repository.patchIncidentStatus(current.id, PatchIncidentStatusRequest(status, null))
                .onSuccess { updated ->
                    _incident.value = updated
                    _selectedStatus.value = updated.status
                    if (message.isNotBlank()) {
                        repository.postComment(current.id, message).getOrNull()
                    }
                    _comments.value = repository.getComments(current.id).getOrElse { _comments.value }
                    _notes.value = ""
                    onSuccess()
                    _updateState.value = UpdateState.Idle
                }
                .onFailure {
                    _incident.value = previousIncident
                    _selectedStatus.value = previousIncident.status
                    _updateState.value = UpdateState.Error("Failed to update. Please try again.")
                }
        }
    }

    fun cancelEdit() {
        _selectedStatus.value = _incident.value?.status
        _notes.value = ""
    }

    fun clearError() { _updateState.value = UpdateState.Idle }
}
