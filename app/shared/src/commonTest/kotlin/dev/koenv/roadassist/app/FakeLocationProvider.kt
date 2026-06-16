package dev.koenv.roadassist.app

import dev.koenv.roadassist.app.location.LocationProvider
import dev.koenv.roadassist.core.location.LatLon

class FakeLocationProvider(private val result: LatLon? = null) : LocationProvider {
    override suspend fun getCurrentLocation(): LatLon? = result
}
