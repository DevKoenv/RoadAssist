package dev.koenv.roadassist.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import dev.koenv.roadassist.app.theme.LocalRoadAssistColors
import dev.koenv.roadassist.app.ui.components.AppDivider
import dev.koenv.roadassist.app.ui.components.CategoryIcon
import dev.koenv.roadassist.app.ui.components.DispatcherNoteCard
import dev.koenv.roadassist.app.ui.components.MobileAppBar
import dev.koenv.roadassist.app.ui.components.StatusBadge
import dev.koenv.roadassist.core.Incident
import dev.koenv.roadassist.core.displayName

@Composable
fun RoadUserDetailScreen(
    viewModel: RoadUserDetailViewModel,
    onBack: () -> Unit,
) {
    val incident by viewModel.incident.collectAsState()
    val loading by viewModel.loading.collectAsState()

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            MobileAppBar(title = "Incident", onBack = onBack)
            AppDivider()
            when {
                loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                incident != null -> RoadUserDetailContent(incident = incident!!)
                else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Incident not found.", style = MaterialTheme.typography.bodyMedium, color = LocalRoadAssistColors.current.mutedForeground)
                }
            }
        }
    }
}

@Composable
internal fun RoadUserDetailContent(incident: Incident) {
    val muted = LocalRoadAssistColors.current.mutedForeground
    val context = LocalPlatformContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier
                    .background(LocalRoadAssistColors.current.muted, RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                CategoryIcon(category = incident.category, modifier = Modifier.size(14.dp))
                Text(incident.category.displayName(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
            }
            StatusBadge(incident.status)
        }

        Text(
            text = incident.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        if (!incident.photoUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(context).data(incident.photoUrl).build(),
                contentDescription = "Incident photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(8.dp)),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(LocalRoadAssistColors.current.muted, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = muted, modifier = Modifier.size(16.dp))
            Text(
                "%.4f, %.4f".format(incident.latitude, incident.longitude),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        if (!incident.notes.isNullOrBlank()) {
            DispatcherNoteCard(notes = incident.notes!!)
        }
    }
}
