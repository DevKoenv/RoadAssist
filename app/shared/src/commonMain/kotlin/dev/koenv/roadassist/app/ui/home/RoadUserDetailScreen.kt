package dev.koenv.roadassist.app.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.List
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
import dev.koenv.roadassist.app.ui.components.AppDesktopShell
import dev.koenv.roadassist.app.ui.components.AppDivider
import dev.koenv.roadassist.app.ui.components.CategoryChip
import dev.koenv.roadassist.app.ui.components.DispatcherNoteCard
import dev.koenv.roadassist.app.ui.components.LocationRow
import dev.koenv.roadassist.app.ui.components.MobileAppBar
import dev.koenv.roadassist.app.ui.components.NavRailItem
import dev.koenv.roadassist.app.ui.components.StatusBadge
import dev.koenv.roadassist.core.Incident

@Composable
fun RoadUserDetailScreen(
    viewModel: RoadUserDetailViewModel,
    onBack: () -> Unit,
    onLogout: () -> Unit,
) {
    val incident by viewModel.incident.collectAsState()
    val loading by viewModel.loading.collectAsState()

    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (maxWidth >= 700.dp) {
            RoadUserDetailDesktopLayout(incident = incident, loading = loading, onBack = onBack, onLogout = onLogout)
        } else {
            RoadUserDetailMobileLayout(incident = incident, loading = loading, onBack = onBack)
        }
    }
}

@Composable
private fun RoadUserDetailMobileLayout(incident: Incident?, loading: Boolean, onBack: () -> Unit) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            MobileAppBar(title = "Incident", onBack = onBack)
            AppDivider()
            DetailBody(incident = incident, loading = loading)
        }
    }
}

@Composable
private fun RoadUserDetailDesktopLayout(
    incident: Incident?,
    loading: Boolean,
    onBack: () -> Unit,
    onLogout: () -> Unit,
) {
    AppDesktopShell(
        onLogout = onLogout,
        navContent = {
            NavRailItem(
                selected = false,
                onClick = onBack,
                icon = { Icon(Icons.Default.List, contentDescription = null, tint = LocalRoadAssistColors.current.mutedForeground) },
                label = "Active",
            )
            NavRailItem(
                selected = false,
                onClick = onBack,
                icon = { Icon(Icons.Default.History, contentDescription = null, tint = LocalRoadAssistColors.current.mutedForeground) },
                label = "History",
            )
        },
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            DesktopDetailHeader(title = "Incident", onBack = onBack)
            AppDivider()
            DetailBody(incident = incident, loading = loading)
        }
    }
}

@Composable
private fun DetailBody(incident: Incident?, loading: Boolean) {
    when {
        loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        incident != null -> RoadUserDetailContent(incident = incident)
        else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Incident not found.", style = MaterialTheme.typography.bodyMedium, color = LocalRoadAssistColors.current.mutedForeground)
        }
    }
}

@Composable
internal fun RoadUserDetailContent(incident: Incident) {
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
            CategoryChip(incident = incident)
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

        LocationRow(latitude = incident.latitude, longitude = incident.longitude)

        if (!incident.notes.isNullOrBlank()) {
            DispatcherNoteCard(notes = incident.notes!!)
        }
    }
}

@Composable
private fun DesktopDetailHeader(title: String, onBack: () -> Unit, subtitle: String? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.AutoMirrored.Default.ArrowBack,
            contentDescription = "Back",
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(20.dp).clickable(onClick = onBack),
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = LocalRoadAssistColors.current.mutedForeground)
            }
        }
    }
}
