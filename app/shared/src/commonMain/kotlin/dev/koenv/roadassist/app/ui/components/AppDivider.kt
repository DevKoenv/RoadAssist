package dev.koenv.roadassist.app.ui.components

import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import dev.koenv.roadassist.app.theme.LocalRoadAssistColors

@Composable
fun AppDivider() {
    HorizontalDivider(color = LocalRoadAssistColors.current.border, thickness = 0.5.dp)
}
