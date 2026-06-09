package dev.koenv.roadassist.app

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
        App()
    }
}
