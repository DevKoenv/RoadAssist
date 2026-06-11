package dev.koenv.roadassist.app.location

import dev.koenv.roadassist.core.LatLon

actual fun createLocationProvider(): LocationProvider = DesktopLocationProvider()

class DesktopLocationProvider : LocationProvider {
    // Desktop has no GPS — location is entered manually via the UI dialog.
    override suspend fun getCurrentLocation(): LatLon? = null
}
