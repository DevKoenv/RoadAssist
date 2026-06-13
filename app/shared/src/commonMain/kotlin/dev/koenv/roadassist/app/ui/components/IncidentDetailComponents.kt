package dev.koenv.roadassist.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.koenv.roadassist.app.theme.LocalRoadAssistColors
import dev.koenv.roadassist.app.util.timeAgo
import dev.koenv.roadassist.core.Incident
import dev.koenv.roadassist.core.IncidentStatus
import dev.koenv.roadassist.core.displayName

@Composable
fun CategoryChip(incident: Incident) {
    val borderColor = LocalRoadAssistColors.current.border
    Row(
        modifier = Modifier
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        CategoryIcon(category = incident.category, modifier = Modifier.size(14.dp))
        Text(incident.category.displayName(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun StatusEditChip(status: IncidentStatus, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalRoadAssistColors.current
    val (bg, fg, label) = when (status) {
        IncidentStatus.NEW -> Triple(colors.statusNewBg, colors.statusNew, "NEW")
        IncidentStatus.IN_PROGRESS -> Triple(colors.statusInProgressBg, colors.statusInProgress, "IN PROGRESS")
        IncidentStatus.EN_ROUTE -> Triple(colors.statusEnRouteBg, colors.statusEnRoute, "EN ROUTE")
        IncidentStatus.RESOLVED -> Triple(colors.statusResolvedBg, colors.statusResolved, "RESOLVED")
    }
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .background(bg, RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(Modifier.size(7.dp).background(fg, CircleShape))
        Text(label, style = MaterialTheme.typography.labelMedium, color = fg)
        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Edit status", tint = fg, modifier = Modifier.size(14.dp))
    }
}

@Composable
fun LocationRow(latitude: Double, longitude: Double) {
    val muted = LocalRoadAssistColors.current.mutedForeground
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(LocalRoadAssistColors.current.muted, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(Icons.Default.LocationOn, contentDescription = null, tint = muted, modifier = Modifier.size(16.dp))
        Column {
            Text("COORDINATES", style = MaterialTheme.typography.labelSmall, color = muted)
            Spacer(Modifier.height(1.dp))
            Text("%.5f, %.5f".format(latitude, longitude), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun IncidentActivitySection(incident: Incident) {
    val muted = LocalRoadAssistColors.current.mutedForeground
    val colors = LocalRoadAssistColors.current
    val nowMillis = remember { System.currentTimeMillis() }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            "ACTIVITY",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.6.sp),
            color = muted,
        )

        ActivityEntry(
            label = "Incident reported",
            timestamp = timeAgo(incident.createdAt, nowMillis),
            dotColor = colors.statusNew,
        )

        if (incident.updatedAt != incident.createdAt) {
            val dotColor = when (incident.status) {
                IncidentStatus.NEW -> colors.statusNew
                IncidentStatus.IN_PROGRESS -> colors.statusInProgress
                IncidentStatus.EN_ROUTE -> colors.statusEnRoute
                IncidentStatus.RESOLVED -> colors.statusResolved
            }
            ActivityEntry(
                label = "Status updated to ${incident.status.displayName()}",
                timestamp = timeAgo(incident.updatedAt, nowMillis),
                dotColor = dotColor,
            )
        }
    }
}

@Composable
private fun ActivityEntry(label: String, timestamp: String, dotColor: Color) {
    val muted = LocalRoadAssistColors.current.mutedForeground
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(Modifier.size(7.dp).background(dotColor, CircleShape))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
            Text(timestamp, style = MaterialTheme.typography.labelSmall, color = muted)
        }
    }
}
