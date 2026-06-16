package dev.koenv.roadassist.app.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dev.koenv.roadassist.app.data.storage.ActivityHolder
import dev.koenv.roadassist.app.data.storage.AndroidContextHolder
import dev.koenv.roadassist.core.location.LatLon
import kotlin.coroutines.resume
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

actual fun createLocationProvider(): LocationProvider = AndroidLocationProvider()

class AndroidLocationProvider : LocationProvider {

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): LatLon? {
        val context = AndroidContextHolder.applicationContext
        val permission = Manifest.permission.ACCESS_FINE_LOCATION

        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            val granted = requestPermission(permission)
            if (!granted) return null
        }

        val client = LocationServices.getFusedLocationProviderClient(context)

        // requestLocationUpdates with maxUpdates(1) actively waits for a fix rather than
        // returning null immediately when no cached location exists (unlike getCurrentLocation).
        // setWaitForAccurateLocation(false) accepts the first available signal (network or GPS).
        val request = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 10_000L)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(0)
            .setMaxUpdates(1)
            .build()

        return suspendCancellableCoroutine { cont ->
            val callback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    cont.resume(result.lastLocation?.let { LatLon(it.latitude, it.longitude) })
                }
            }
            client.requestLocationUpdates(request, callback, Looper.getMainLooper())
                .addOnFailureListener { cont.resume(null) }
            cont.invokeOnCancellation { client.removeLocationUpdates(callback) }
        }
    }

    private suspend fun requestPermission(permission: String): Boolean {
        val activity = ActivityHolder.activity ?: return false

        val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
        if (shouldShowRationale) {
            val accepted = withContext(Dispatchers.Main) {
                suspendCancellableCoroutine { cont ->
                    val dialog = android.app.AlertDialog.Builder(activity)
                        .setTitle("Location required")
                        .setMessage("RoadAssist needs your location to report an incident accurately.")
                        .setPositiveButton("OK") { _, _ -> cont.resume(true) }
                        .setNegativeButton("Cancel") { _, _ -> cont.resume(false) }
                        .setOnCancelListener { cont.resume(false) }
                        .show()
                    cont.invokeOnCancellation { dialog.dismiss() }
                }
            }
            if (!accepted) return false
        }

        val launcher = ActivityHolder.launchPermissionRequest ?: return false
        val deferred = CompletableDeferred<Boolean>()
        ActivityHolder.locationPermissionDeferred = deferred
        withContext(Dispatchers.Main) { launcher.invoke(permission) }
        return deferred.await()
    }
}
