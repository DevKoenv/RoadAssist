package dev.koenv.roadassist.app.geocoding

import dev.koenv.roadassist.core.location.LatLon

interface GeocodingService {
    suspend fun search(query: String): List<GeocodingResult>
    suspend fun reverse(location: LatLon): String?
}
