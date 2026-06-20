package dev.koenv.roadassist.app.geocoding

import dev.koenv.roadassist.app.geocoding.GeocodingResult
import dev.koenv.roadassist.app.geocoding.GeocodingService
import dev.koenv.roadassist.core.location.LatLon

class FakeGeocodingService(
    private val searchResults: List<GeocodingResult> = emptyList(),
    private val reverseResult: String? = null,
) : GeocodingService {
    override suspend fun search(query: String): List<GeocodingResult> = searchResults
    override suspend fun reverse(location: LatLon): String? = reverseResult
}
