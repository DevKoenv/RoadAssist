package dev.koenv.roadassist.app

import dev.koenv.roadassist.app.media.MediaPicker

class FakeMediaPicker(private val result: ByteArray? = null) : MediaPicker {
    override suspend fun pickMedia(): ByteArray? = result
}
