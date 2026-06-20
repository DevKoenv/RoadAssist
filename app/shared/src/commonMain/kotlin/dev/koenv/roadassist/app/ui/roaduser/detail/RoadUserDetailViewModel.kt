package dev.koenv.roadassist.app.ui.roaduser.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.koenv.roadassist.app.data.incidents.IncidentRepository
import dev.koenv.roadassist.app.data.sse.EventStreamService
import dev.koenv.roadassist.app.geocoding.GeocodingService
import dev.koenv.roadassist.core.comment.Comment
import dev.koenv.roadassist.core.incident.Incident
import dev.koenv.roadassist.core.location.LatLon
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RoadUserDetailViewModel(
    private val repository: IncidentRepository,
    private val incidentId: Int,
    private val geocodingService: GeocodingService? = null,
    private val eventStreamService: EventStreamService,
) : ViewModel() {

    val incident: StateFlow<Incident?> = repository.observeIncident(incidentId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    val comments: StateFlow<List<Comment>> = repository.observeComments(incidentId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _address = MutableStateFlow<String?>(null)
    val address: StateFlow<String?> = _address.asStateFlow()

    private val _commentInput = MutableStateFlow("")
    val commentInput: StateFlow<String> = _commentInput.asStateFlow()

    private val _commentPosting = MutableStateFlow(false)
    val commentPosting: StateFlow<Boolean> = _commentPosting.asStateFlow()

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    val serverReachable: StateFlow<Boolean> = eventStreamService.serverReachable

    init {
        viewModelScope.launch {
            repository.syncIncident(incidentId)
            _loading.value = false
        }
        viewModelScope.launch {
            val inc = incident.filterNotNull().first()
            if (geocodingService != null) {
                _address.value = geocodingService.reverse(LatLon(inc.latitude, inc.longitude))
            }
        }
        viewModelScope.launch {
            eventStreamService.reconnects.collect {
                repository.syncIncident(incidentId)
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
}
