package dev.koenv.roadassist.app.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import dev.koenv.roadassist.app.theme.LocalRoadAssistColors

@Composable
fun LogoutTextButton(onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(
            text = "Log out",
            style = MaterialTheme.typography.bodySmall,
            color = LocalRoadAssistColors.current.mutedForeground,
        )
    }
}
