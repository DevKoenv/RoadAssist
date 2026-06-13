package dev.koenv.roadassist.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.koenv.roadassist.app.theme.LocalRoadAssistColors
import dev.koenv.roadassist.core.Incident
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
            Text("LOCATION", style = MaterialTheme.typography.labelSmall, color = muted)
            Spacer(Modifier.height(1.dp))
            Text("%.4f, %.4f".format(latitude, longitude), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
