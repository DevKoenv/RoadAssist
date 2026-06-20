package dev.koenv.roadassist.app.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import dev.koenv.roadassist.app.theme.LocalRoadAssistColors

@Composable
fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.6.sp),
        color = LocalRoadAssistColors.current.mutedForeground,
    )
}
