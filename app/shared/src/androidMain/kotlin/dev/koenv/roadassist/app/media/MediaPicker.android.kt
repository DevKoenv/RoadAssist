package dev.koenv.roadassist.app.media

import android.net.Uri
import androidx.core.content.FileProvider
import dev.koenv.roadassist.app.data.storage.ActivityHolder
import dev.koenv.roadassist.app.data.storage.AndroidContextHolder
import java.io.File
import kotlin.coroutines.resume
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

actual fun createMediaPicker(): MediaPicker = AndroidMediaPicker()

@Suppress("TooGenericExceptionCaught", "SwallowedException")
class AndroidMediaPicker : MediaPicker {

    override suspend fun pickMedia(): ByteArray? {
        val activity = ActivityHolder.activity ?: return null
        val context = AndroidContextHolder.applicationContext

        val choice = withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { cont ->
                val dialog = android.app.AlertDialog.Builder(activity)
                    .setTitle("Add photo")
                    .setItems(arrayOf("Camera", "Gallery")) { _, which -> cont.resume(which) }
                    .setNegativeButton("Cancel") { _, _ -> cont.resume(-1) }
                    .setOnCancelListener { cont.resume(-1) }
                    .show()
                cont.invokeOnCancellation { dialog.dismiss() }
            }
        }

        return when (choice) {
            0 -> takeCameraPhoto(context)
            1 -> pickFromGallery(context)
            else -> null
        }
    }

    private suspend fun takeCameraPhoto(context: android.content.Context): ByteArray? {
        val imageFile = File(context.cacheDir, "camera_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile,
        )
        ActivityHolder.cameraImageUri = uri
        val launcher = ActivityHolder.launchCamera ?: return null
        val deferred = CompletableDeferred<Boolean>()
        ActivityHolder.cameraDeferred = deferred
        withContext(Dispatchers.Main) { launcher.invoke(uri) }
        val success = deferred.await()
        return if (success) imageFile.readBytes() else null
    }

    private suspend fun pickFromGallery(context: android.content.Context): ByteArray? {
        val launcher = ActivityHolder.launchGallery ?: return null
        val deferred = CompletableDeferred<Uri?>()
        ActivityHolder.galleryDeferred = deferred
        withContext(Dispatchers.Main) { launcher.invoke("image/*") }
        val uri = deferred.await() ?: return null
        return try {
            context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        } catch (e: Exception) {
            null
        }
    }
}
