package dev.koenv.roadassist.app.media

/**
 * Lets the user select or capture a photo to attach to an incident report.
 *
 * Android: shows a dialog offering "Camera" or "Gallery". Camera captures via an intent using
 * a [FileProvider] URI (required on API 24+ to share file URIs with external apps). Gallery
 * opens the system media picker and reads the selected image through the content resolver.
 *
 * Desktop (JVM): opens a [javax.swing.JFileChooser] dialog filtered to JPEG and PNG files.
 * The dialog runs on the Swing EDT; a [java.util.concurrent.CountDownLatch] suspends the
 * coroutine until the user dismisses the dialog without blocking the EDT.
 *
 * Returns the raw image bytes, or null if the user cancels or an error occurs.
 */
interface MediaPicker {
    /** Suspends until the user picks or captures an image. Returns the bytes or null on cancel. */
    suspend fun pickMedia(): ByteArray?
}

/**
 * Returns the platform-appropriate [MediaPicker] implementation.
 * Call once per screen that needs media selection.
 */
expect fun createMediaPicker(): MediaPicker
