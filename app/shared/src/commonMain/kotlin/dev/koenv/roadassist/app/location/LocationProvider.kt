package dev.koenv.roadassist.app.location

import dev.koenv.roadassist.core.location.LatLon

interface LocationProvider {
    suspend fun getCurrentLocation(): LatLon?
}

expect fun createLocationProvider(): LocationProvider
