package dev.koenv.roadassist.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.koenv.roadassist.app.theme.LocalRoadAssistColors
import dev.koenv.roadassist.app.util.timeAgo
import dev.koenv.roadassist.core.Incident
import dev.koenv.roadassist.core.displayName

@Composable
fun IncidentListItem(
    incident: Incident,
    nowMillis: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
) {
    val muted = LocalRoadAssistColors.current.mutedForeground
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(LocalRoadAssistColors.current.muted, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            CategoryIcon(category = incident.category, modifier = Modifier.size(18.dp))
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    incident.category.displayName(),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                StatusBadge(incident.status)
            }
            Text(
                incident.description,
                style = MaterialTheme.typography.bodySmall,
                color = muted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    timeAgo(incident.createdAt, nowMillis),
                    style = MaterialTheme.typography.labelSmall,
                    color = muted,
                )
                trailing?.invoke()
            }
        }
    }
}
