package dev.koenv.roadassist.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.koenv.roadassist.app.theme.LocalRoadAssistColors

@Composable
fun AppDesktopShell(
    onLogout: () -> Unit,
    navContent: @Composable ColumnScope.() -> Unit,
    content: @Composable () -> Unit,
) {
    Row(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        AppNavRail(onLogout = onLogout, content = navContent)
        Box(
            modifier = Modifier
                .width(0.5.dp)
                .fillMaxHeight()
                .background(LocalRoadAssistColors.current.border),
        )
        Box(modifier = Modifier.weight(1f).fillMaxSize()) {
            content()
        }
    }
}
