package dev.koenv.roadassist.app.media

interface MediaPicker {
    suspend fun pickMedia(): ByteArray?
}

expect fun createMediaPicker(): MediaPicker
