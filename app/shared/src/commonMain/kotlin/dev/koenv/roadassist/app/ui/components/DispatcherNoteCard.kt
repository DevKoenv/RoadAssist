package dev.koenv.roadassist.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.koenv.roadassist.app.theme.LocalRoadAssistColors

@Composable
fun DispatcherNoteCard(notes: String, modifier: Modifier = Modifier) {
    val colors = LocalRoadAssistColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.accent, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            "DISPATCHER NOTE",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.6.sp),
            color = colors.accentForeground,
        )
        Text(
            notes,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
