package dev.koenv.roadassist.app.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.koenv.roadassist.app.theme.LocalRoadAssistColors
import dev.koenv.roadassist.core.IncidentCategory
import dev.koenv.roadassist.core.displayName

@Composable
fun CategoryIcon(
    category: IncidentCategory,
    tint: Color = LocalRoadAssistColors.current.mutedForeground,
    modifier: Modifier = Modifier.size(18.dp),
) {
    val icon = when (category) {
        IncidentCategory.BREAKDOWN -> Icons.Default.Build
        IncidentCategory.ACCIDENT -> Icons.Default.WarningAmber
        IncidentCategory.OBSTRUCTION -> Icons.Default.Lock
        IncidentCategory.OTHER -> Icons.AutoMirrored.Filled.Help
    }
    Icon(icon, contentDescription = category.displayName(), tint = tint, modifier = modifier)
}
