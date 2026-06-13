package dev.koenv.roadassist.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import dev.koenv.roadassist.app.data.incidents.IncidentRepository
import dev.koenv.roadassist.app.theme.LocalRoadAssistColors
import dev.koenv.roadassist.app.ui.components.AppDivider
import dev.koenv.roadassist.app.ui.components.MobileAppBar
import dev.koenv.roadassist.app.ui.components.SectionLabel
import dev.koenv.roadassist.core.Incident

@Composable
fun RoadUserDetailScreen(
    incidentId: Int,
    repository: IncidentRepository,
    onBack: () -> Unit,
) {
    val incident by produceState<Incident?>(null, incidentId) {
        value = repository.getIncident(incidentId).getOrNull()
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            MobileAppBar(title = "Incident detail", onBack = onBack)
            AppDivider()
            when (val current = incident) {
                null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                else -> RoadUserDetailContent(incident = current)
            }
        }
    }
}

@Composable
private fun RoadUserDetailContent(incident: Incident) {
    val mutedColor = LocalRoadAssistColors.current.mutedForeground
    val context = LocalPlatformContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionLabel("CATEGORY")
        Text(
            text = incident.category.name.lowercase().replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        SectionLabel("DESCRIPTION")
        Text(
            text = incident.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        SectionLabel("LOCATION")
        Text(
            text = "%.6f, %.6f".format(incident.latitude, incident.longitude),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        SectionLabel("STATUS")
        Text(
            text = incident.status.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        if (!incident.photoUrl.isNullOrBlank()) {
            SectionLabel("PHOTO")
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(incident.photoUrl)
                    .build(),
                contentDescription = "Incident photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(8.dp)),
            )
        }

        if (!incident.notes.isNullOrBlank()) {
            SectionLabel("DISPATCHER NOTES")
            Text(
                text = incident.notes!!,
                style = MaterialTheme.typography.bodyMedium,
                color = mutedColor,
            )
        }
    }
}
