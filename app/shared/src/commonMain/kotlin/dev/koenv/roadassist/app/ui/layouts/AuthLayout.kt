package dev.koenv.roadassist.app.ui.layouts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.koenv.roadassist.app.ui.foundation.LocalWindowSizeClass
import dev.koenv.roadassist.app.ui.foundation.WindowSizeClass

@Composable
fun AuthLayout(content: @Composable () -> Unit) {
    val windowSizeClass = LocalWindowSizeClass.current
    if (windowSizeClass == WindowSizeClass.Compact) {
        // mobile: full-screen login form
        Box(Modifier.fillMaxSize()) { content() }
    } else {
        // desktop: branding panel on left, login form on right
        Row(Modifier.fillMaxSize()) {
            AuthBrandingPanel(Modifier.weight(1f)) // left: dark branded sidebar
            Box(Modifier.weight(1f)) { content() } // right: login form panel
        }
    }
}
