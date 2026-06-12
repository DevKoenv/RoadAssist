package dev.koenv.roadassist.app.geocoding

import dev.koenv.roadassist.core.LatLon

class NoOpGeocodingService : GeocodingService {
    override suspend fun search(query: String): List<GeocodingResult> = emptyList()
    override suspend fun reverse(location: LatLon): String? = null
}
