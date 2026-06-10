package dev.koenv.roadassist.app

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.jetbrains.compose.resources.painterResource
import roadassist.app.shared.generated.resources.Res
import roadassist.app.shared.generated.resources.icon_dark

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "RoadAssist",
        icon = painterResource(Res.drawable.icon_dark),
    ) {
        Box(Modifier.fillMaxSize().border(1.dp, Color(0xFFE4E7EC))) {
            App()
        }
    }
}
