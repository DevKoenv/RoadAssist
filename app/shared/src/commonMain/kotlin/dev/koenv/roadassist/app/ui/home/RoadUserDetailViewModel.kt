package dev.koenv.roadassist.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.koenv.roadassist.app.data.incidents.IncidentRepository
import dev.koenv.roadassist.core.Incident
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RoadUserDetailViewModel(
    private val repository: IncidentRepository,
    private val incidentId: Int,
) : ViewModel() {

    private val _incident = MutableStateFlow<Incident?>(null)
    val incident: StateFlow<Incident?> = _incident.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    init {
        viewModelScope.launch {
            _incident.value = repository.getIncident(incidentId).getOrNull()
            _loading.value = false
        }
    }
}
