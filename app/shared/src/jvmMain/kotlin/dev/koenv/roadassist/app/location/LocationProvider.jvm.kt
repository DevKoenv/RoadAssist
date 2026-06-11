package dev.koenv.roadassist.app.location

import dev.koenv.roadassist.core.LatLon
import java.awt.BorderLayout
import java.awt.GridLayout
import java.util.concurrent.CountDownLatch
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.SwingUtilities
import javax.swing.WindowConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual fun createLocationProvider(): LocationProvider = DesktopLocationProvider()

class DesktopLocationProvider : LocationProvider {
    override suspend fun getCurrentLocation(): LatLon? = withContext(Dispatchers.IO) {
        var result: LatLon? = null
        val latch = CountDownLatch(1)

        SwingUtilities.invokeLater {
            val dialog = JDialog(null as java.awt.Frame?, "Enter Location", true)
            dialog.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE

            val latField = JTextField(10)
            val lonField = JTextField(10)
            val panel = JPanel(GridLayout(2, 2, 8, 8))
            panel.border = BorderFactory.createEmptyBorder(16, 16, 8, 16)
            panel.add(JLabel("Latitude:"))
            panel.add(latField)
            panel.add(JLabel("Longitude:"))
            panel.add(lonField)

            val okBtn = JButton("OK")
            val cancelBtn = JButton("Cancel")
            val btnPanel = JPanel()
            btnPanel.add(okBtn)
            btnPanel.add(cancelBtn)

            okBtn.addActionListener {
                val lat = latField.text.toDoubleOrNull()
                val lon = lonField.text.toDoubleOrNull()
                if (lat != null && lon != null) result = LatLon(lat, lon)
                dialog.dispose()
            }
            cancelBtn.addActionListener {
                dialog.dispose()
            }
            dialog.addWindowListener(object : java.awt.event.WindowAdapter() {
                override fun windowClosed(e: java.awt.event.WindowEvent?) = latch.countDown()
            })

            dialog.add(panel, BorderLayout.CENTER)
            dialog.add(btnPanel, BorderLayout.SOUTH)
            dialog.pack()
            dialog.setLocationRelativeTo(null)
            dialog.isVisible = true
        }

        latch.await()
        result
    }
}
