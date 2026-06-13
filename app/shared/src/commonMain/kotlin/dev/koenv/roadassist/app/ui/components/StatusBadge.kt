package dev.koenv.roadassist.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.koenv.roadassist.app.theme.LocalRoadAssistColors
import dev.koenv.roadassist.core.IncidentStatus

@Composable
fun StatusBadge(status: IncidentStatus, modifier: Modifier = Modifier) {
    val colors = LocalRoadAssistColors.current
    val (bg, fg, label) = when (status) {
        IncidentStatus.NEW -> Triple(colors.statusNewBg, colors.statusNew, "NEW")
        IncidentStatus.IN_PROGRESS -> Triple(colors.statusInProgressBg, colors.statusInProgress, "IN PROGRESS")
        IncidentStatus.EN_ROUTE -> Triple(colors.statusEnRouteBg, colors.statusEnRoute, "EN ROUTE")
        IncidentStatus.RESOLVED -> Triple(colors.statusResolvedBg, colors.statusResolved, "RESOLVED")
    }
    StatusBadgeContent(bg = bg, fg = fg, label = label, modifier = modifier)
}

@Composable
private fun StatusBadgeContent(bg: Color, fg: Color, label: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(bg, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(Modifier.size(6.dp).background(fg, CircleShape))
        Text(label, style = MaterialTheme.typography.labelSmall, color = fg)
    }
}
