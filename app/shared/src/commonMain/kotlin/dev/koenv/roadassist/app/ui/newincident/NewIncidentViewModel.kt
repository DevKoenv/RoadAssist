package dev.koenv.roadassist.app.ui.newincident

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.koenv.roadassist.app.data.incidents.IncidentRepository
import dev.koenv.roadassist.app.geocoding.GeocodingResult
import dev.koenv.roadassist.app.geocoding.GeocodingService
import dev.koenv.roadassist.app.location.LocationProvider
import dev.koenv.roadassist.app.media.MediaPicker
import dev.koenv.roadassist.core.CreateIncidentRequest
import dev.koenv.roadassist.core.IncidentCategory
import dev.koenv.roadassist.core.LatLon
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class NewIncidentViewModel(
    private val repository: IncidentRepository,
    private val locationProvider: LocationProvider,
    private val mediaPicker: MediaPicker,
    private val geocodingService: GeocodingService,
) : ViewModel() {

    private val _category = MutableStateFlow(IncidentCategory.BREAKDOWN)
    val category: StateFlow<IncidentCategory> = _category.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _location = MutableStateFlow<LatLon?>(null)
    val location: StateFlow<LatLon?> = _location.asStateFlow()

    private val _locationLabel = MutableStateFlow<String?>(null)
    val locationLabel: StateFlow<String?> = _locationLabel.asStateFlow()

    private val _locationLoading = MutableStateFlow(false)
    val locationLoading: StateFlow<Boolean> = _locationLoading.asStateFlow()

    private val _photoBytes = MutableStateFlow<ByteArray?>(null)
    val photoBytes: StateFlow<ByteArray?> = _photoBytes.asStateFlow()

    private val _submitState = MutableStateFlow<SubmitState>(SubmitState.Idle)
    val submitState: StateFlow<SubmitState> = _submitState.asStateFlow()

    private val _searchResults = MutableStateFlow<List<GeocodingResult>>(emptyList())
    val searchResults: StateFlow<List<GeocodingResult>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private var locationSetManually = false

    init {
        viewModelScope.launch {
            val latLon = withTimeoutOrNull(10_000L) { locationProvider.getCurrentLocation() }
            if (latLon != null && !locationSetManually) {
                _location.value = latLon
                val label = geocodingService.reverse(latLon)
                if (!locationSetManually) {
                    _locationLabel.value = label
                }
            }
        }
    }

    fun updateCategory(value: IncidentCategory) {
        _category.value = value
    }

    fun updateDescription(value: String) {
        _description.value = if (value.length <= 500) value else value.take(500)
    }

    fun refreshLocation() {
        viewModelScope.launch {
            _locationLoading.value = true
            val latLon = withTimeoutOrNull(15_000L) { locationProvider.getCurrentLocation() }
            _location.value = latLon
            _locationLabel.value = if (latLon != null) geocodingService.reverse(latLon) else null
            _locationLoading.value = false
        }
    }

    fun setManualLocation(lat: Double, lon: Double, label: String) {
        locationSetManually = true
        _location.value = LatLon(lat, lon)
        _locationLabel.value = label
    }

    fun searchLocations(query: String) {
        viewModelScope.launch {
            _isSearching.value = true
            _searchResults.value = geocodingService.search(query)
            _isSearching.value = false
        }
    }

    fun clearSearch() {
        _searchResults.value = emptyList()
        _isSearching.value = false
    }

    fun pickPhoto() {
        viewModelScope.launch {
            _photoBytes.value = mediaPicker.pickMedia()
        }
    }

    fun removePhoto() {
        _photoBytes.value = null
    }

    fun submit() {
        val loc = _location.value
        if (loc == null) {
            _submitState.value = SubmitState.Error("Location is required")
            return
        }
        if (_description.value.isBlank()) {
            _submitState.value = SubmitState.Error("Description is required")
            return
        }
        viewModelScope.launch {
            _submitState.value = SubmitState.Loading
            val request = CreateIncidentRequest(
                category = _category.value,
                description = _description.value,
                latitude = loc.latitude,
                longitude = loc.longitude,
            )
            repository.createIncident(request).fold(
                onSuccess = { incident ->
                    val bytes = _photoBytes.value
                    if (bytes != null) {
                        repository.uploadPhoto(incident.id, bytes, "image/jpeg")
                    }
                    _submitState.value = SubmitState.Success
                },
                onFailure = {
                    _submitState.value = SubmitState.Error("Failed to report incident")
                },
            )
        }
    }
}
