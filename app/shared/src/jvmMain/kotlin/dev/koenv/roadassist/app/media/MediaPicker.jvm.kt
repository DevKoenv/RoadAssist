package dev.koenv.roadassist.app.media

import java.util.concurrent.CountDownLatch
import javax.swing.JFileChooser
import javax.swing.SwingUtilities
import javax.swing.filechooser.FileNameExtensionFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual fun createMediaPicker(): MediaPicker = DesktopMediaPicker()

class DesktopMediaPicker : MediaPicker {
    override suspend fun pickMedia(): ByteArray? = withContext(Dispatchers.IO) {
        var result: ByteArray? = null
        val latch = CountDownLatch(1)

        SwingUtilities.invokeLater {
            val chooser = JFileChooser().apply {
                dialogTitle = "Select photo"
                fileSelectionMode = JFileChooser.FILES_ONLY
                fileFilter = FileNameExtensionFilter("Images (JPEG, PNG)", "jpg", "jpeg", "png")
                isAcceptAllFileFilterUsed = false
            }
            val returnValue = chooser.showOpenDialog(null)
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                result = chooser.selectedFile.readBytes()
            }
            latch.countDown()
        }

        latch.await()
        result
    }
}
