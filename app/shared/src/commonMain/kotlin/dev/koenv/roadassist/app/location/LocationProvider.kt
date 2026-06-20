package dev.koenv.roadassist.app.location

import dev.koenv.roadassist.core.location.LatLon

/**
 * Retrieves the device's current geographic coordinates.
 *
 * Android: uses the Fused Location Provider (Google Play Services). Requests a single location
 * update with BALANCED_POWER_ACCURACY and accepts the first available fix (network or GPS).
 * Prompts the user for ACCESS_FINE_LOCATION permission if not already granted. Returns null if
 * the permission is denied or if the location request fails.
 *
 * Desktop (JVM): the desktop platform has no GPS hardware. Always returns null; the user
 * is expected to enter the location manually via the address search dialog in the UI.
 */
interface LocationProvider {
    /** Returns the current [LatLon], or null if the location could not be determined. */
    suspend fun getCurrentLocation(): LatLon?
}

/**
 * Returns the platform-appropriate [LocationProvider] implementation.
 * Call once per screen that needs location access.
 */
expect fun createLocationProvider(): LocationProvider
