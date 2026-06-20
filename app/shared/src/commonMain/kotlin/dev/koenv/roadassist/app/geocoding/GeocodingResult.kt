package dev.koenv.roadassist.app.geocoding

import dev.koenv.roadassist.core.location.LatLon

data class GeocodingResult(val label: String, val location: LatLon)
