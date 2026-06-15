package dev.koenv.roadassist.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.koenv.roadassist.app.data.incidents.IncidentRepository
import dev.koenv.roadassist.app.geocoding.GeocodingService
import dev.koenv.roadassist.core.Comment
import dev.koenv.roadassist.core.Incident
import dev.koenv.roadassist.core.LatLon
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RoadUserDetailViewModel(
    private val repository: IncidentRepository,
    private val incidentId: Int,
    private val geocodingService: GeocodingService? = null,
) : ViewModel() {

    private val _incident = MutableStateFlow<Incident?>(null)
    val incident: StateFlow<Incident?> = _incident.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

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
            repository.observeIncident(incidentId).collect { incident ->
                _incident.value = incident
                _loading.value = false
                if (incident != null && geocodingService != null && _address.value == null) {
                    _address.value = geocodingService.reverse(LatLon(incident.latitude, incident.longitude))
                }
            }
        }
        viewModelScope.launch {
            repository.observeComments(incidentId).collect { _comments.value = it }
        }
        viewModelScope.launch {
            repository.syncIncident(incidentId)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _refreshing.value = true
            repository.syncIncident(incidentId)
            _refreshing.value = false
        }
    }

    fun updateCommentInput(text: String) {
        _commentInput.value = text
    }

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
}
