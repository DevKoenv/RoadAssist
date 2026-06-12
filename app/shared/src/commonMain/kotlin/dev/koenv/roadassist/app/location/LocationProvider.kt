package dev.koenv.roadassist.app.location

import dev.koenv.roadassist.core.LatLon

interface LocationProvider {
    suspend fun getCurrentLocation(): LatLon?
}

expect fun createLocationProvider(): LocationProvider
