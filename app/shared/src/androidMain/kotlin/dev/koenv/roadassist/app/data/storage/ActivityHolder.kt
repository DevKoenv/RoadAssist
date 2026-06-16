package dev.koenv.roadassist.app.data.storage

import android.app.Activity
import android.net.Uri
import kotlinx.coroutines.CompletableDeferred

// Shared KMP code can't hold an Activity reference, so we park the ActivityResultLaunchers
// here and wire them up from MainActivity. Clear activity in onDestroy to avoid leaking it.
object ActivityHolder {
    var activity: Activity? = null
    var launchPermissionRequest: ((String) -> Unit)? = null
    var launchCamera: ((Uri) -> Unit)? = null
    var launchGallery: ((String) -> Unit)? = null
    var cameraImageUri: Uri? = null
    var locationPermissionDeferred: CompletableDeferred<Boolean>? = null
    var cameraDeferred: CompletableDeferred<Boolean>? = null
    var galleryDeferred: CompletableDeferred<Uri?>? = null
}
