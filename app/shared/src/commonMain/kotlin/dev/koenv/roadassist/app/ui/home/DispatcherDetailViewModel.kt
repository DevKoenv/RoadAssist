package dev.koenv.roadassist.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.koenv.roadassist.app.data.incidents.IncidentRepository
import dev.koenv.roadassist.core.Incident
import dev.koenv.roadassist.core.IncidentStatus
import dev.koenv.roadassist.core.PatchIncidentStatusRequest
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

    init {
        viewModelScope.launch {
            val result = repository.getIncident(incidentId).getOrNull()
            _incident.value = result
            _selectedStatus.value = result?.status
            _notes.value = result?.notes.orEmpty()
            _loading.value = false
        }
    }

    fun selectStatus(status: IncidentStatus) { _selectedStatus.value = status }

    fun updateNotes(notes: String) { _notes.value = notes }

    fun saveUpdate(onSuccess: () -> Unit) {
        val current = _incident.value ?: return
        val status = _selectedStatus.value ?: return
        val notes = _notes.value

        val previousIncident = current

        viewModelScope.launch {
            _updateState.value = UpdateState.Loading
            _incident.value = current.copy(status = status, notes = notes)

            repository.patchIncidentStatus(current.id, PatchIncidentStatusRequest(status, notes))
                .onSuccess { updated ->
                    _incident.value = updated
                    _selectedStatus.value = updated.status
                    _notes.value = updated.notes.orEmpty()
                    _updateState.value = UpdateState.Idle
                    onSuccess()
                }
                .onFailure {
                    _incident.value = previousIncident
                    _selectedStatus.value = previousIncident.status
                    _notes.value = previousIncident.notes.orEmpty()
                    _updateState.value = UpdateState.Error("Failed to update. Please try again.")
                }
        }
    }

    fun clearError() { _updateState.value = UpdateState.Idle }
}
