package dev.koenv.roadassist.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import dev.koenv.roadassist.app.theme.LocalRoadAssistColors
import dev.koenv.roadassist.app.theme.RoadAssistColors
import dev.koenv.roadassist.app.ui.components.AppDesktopShell
import dev.koenv.roadassist.app.ui.components.AppDivider
import dev.koenv.roadassist.app.ui.components.ConnectivityBanner
import dev.koenv.roadassist.app.ui.components.DesktopPageHeader
import dev.koenv.roadassist.app.ui.components.EmptyState
import dev.koenv.roadassist.app.ui.components.IncidentListItem
import dev.koenv.roadassist.app.ui.components.LogoutTextButton
import dev.koenv.roadassist.app.ui.components.MobileAppBar
import dev.koenv.roadassist.app.ui.components.NavRailItem
import dev.koenv.roadassist.core.Incident
import dev.koenv.roadassist.core.IncidentStatus

private enum class DispatcherFilter { All, New, InProgress, EnRoute, Resolved }

@Composable
fun DispatcherHomeScreen(
    viewModel: HomeViewModel,
    onLogout: () -> Unit,
    onIncidentClick: (Int) -> Unit,
) {
    val serverReachable by viewModel.serverReachable.collectAsState()
    val incidents by viewModel.incidents.collectAsState()
    val onLogoutClick: () -> Unit = { viewModel.logout(onLogout) }
    val nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (maxWidth >= 700.dp) {
            DispatcherDesktopLayout(
                incidents = incidents,
                serverReachable = serverReachable,
                nowMillis = nowMillis,
                onLogout = onLogoutClick,
                onIncidentClick = onIncidentClick,
                onRefresh = viewModel::refreshIncidents,
            )
        } else {
            DispatcherMobileLayout(
                incidents = incidents,
                serverReachable = serverReachable,
                nowMillis = nowMillis,
                onLogout = onLogoutClick,
                onIncidentClick = onIncidentClick,
                onRefresh = viewModel::refreshIncidents,
            )
        }
    }
}

@Composable
private fun DispatcherMobileLayout(
    incidents: List<Incident>,
    serverReachable: Boolean,
    nowMillis: Long,
    onLogout: () -> Unit,
    onIncidentClick: (Int) -> Unit,
    onRefresh: () -> Unit,
) {
    var filter by remember { mutableStateOf(DispatcherFilter.All) }
    val filtered = filterIncidents(incidents, filter)

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            ConnectivityBanner(visible = !serverReachable)
            MobileAppBar(
                title = "All incidents",
                trailing = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = LocalRoadAssistColors.current.mutedForeground)
                    }
                    LogoutTextButton(onClick = onLogout)
                },
            )
            StatusFilterRow(filter = filter, onFilterChange = { filter = it })
            AppDivider()
            if (filtered.isEmpty()) {
                EmptyState("No incidents in queue", "New reports from road users will appear here.")
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filtered, key = { it.id }) { incident ->
                        IncidentListItem(
                            incident = incident,
                            nowMillis = nowMillis,
                            onClick = { onIncidentClick(incident.id) },
                            trailing = { UserIdBadge(incident.userId) },
                        )
                        AppDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun DispatcherDesktopLayout(
    incidents: List<Incident>,
    serverReachable: Boolean,
    nowMillis: Long,
    onLogout: () -> Unit,
    onIncidentClick: (Int) -> Unit,
    onRefresh: () -> Unit,
) {
    var filter by remember { mutableStateOf(DispatcherFilter.All) }
    val filtered = filterIncidents(incidents, filter)

    AppDesktopShell(
        onLogout = onLogout,
        navContent = {
            NavRailItem(
                selected = true,
                onClick = {},
                icon = { QueueIcon(selected = true) },
                label = "Queue",
            )
        },
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ConnectivityBanner(visible = !serverReachable)
            DesktopPageHeader(
                title = "All incidents",
                trailing = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = LocalRoadAssistColors.current.mutedForeground)
                    }
                },
            )
            StatusFilterRow(filter = filter, onFilterChange = { filter = it }, modifier = Modifier.padding(horizontal = 24.dp))
            AppDivider()
            if (filtered.isEmpty()) {
                EmptyState("No incidents in queue", "New reports from road users will appear here.")
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filtered, key = { it.id }) { incident ->
                        IncidentListItem(
                            incident = incident,
                            nowMillis = nowMillis,
                            onClick = { onIncidentClick(incident.id) },
                            trailing = { UserIdBadge(incident.userId) },
                        )
                        AppDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun UserIdBadge(userId: Int) {
    val muted = LocalRoadAssistColors.current.mutedForeground
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Icon(Icons.Default.Person, contentDescription = null, tint = muted, modifier = Modifier.size(11.dp))
        Text("u-$userId", style = MaterialTheme.typography.labelSmall, color = muted)
    }
}

@Composable
private fun StatusFilterRow(
    filter: DispatcherFilter,
    onFilterChange: (DispatcherFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    val extColors = LocalRoadAssistColors.current
    LazyRow(
        modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(DispatcherFilter.entries) { f ->
            FilterChip(
                selected = filter == f,
                onClick = { onFilterChange(f) },
                label = {
                    Text(
                        text = when (f) {
                            DispatcherFilter.All -> "All"
                            DispatcherFilter.New -> "New"
                            DispatcherFilter.InProgress -> "In progress"
                            DispatcherFilter.EnRoute -> "En route"
                            DispatcherFilter.Resolved -> "Resolved"
                        },
                        style = MaterialTheme.typography.labelMedium,
                    )
                },
                shape = RoundedCornerShape(7.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = RoadAssistColors.Accent,
                    selectedLabelColor = MaterialTheme.colorScheme.primary,
                    containerColor = extColors.muted,
                    labelColor = extColors.mutedForeground,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = filter == f,
                    borderColor = extColors.border,
                    selectedBorderColor = Color.Transparent,
                    borderWidth = 0.5.dp,
                    selectedBorderWidth = 0.dp,
                ),
            )
        }
    }
}

@Composable
private fun QueueIcon(selected: Boolean) {
    val color = if (selected) MaterialTheme.colorScheme.primary else LocalRoadAssistColors.current.mutedForeground
    Box(
        modifier = Modifier.size(22.dp).drawBehind {
            val w = size.width
            val h = size.height
            val stroke = w * 0.1f
            for (i in 0..2) {
                val y = h * 0.22f + i * h * 0.26f
                drawLine(color = color, start = Offset(w * 0.08f, y), end = Offset(w * 0.92f, y), strokeWidth = stroke, cap = StrokeCap.Round)
            }
        },
    ) { }
}

private fun filterIncidents(incidents: List<Incident>, filter: DispatcherFilter): List<Incident> =
    when (filter) {
        DispatcherFilter.All -> incidents
        DispatcherFilter.New -> incidents.filter { it.status == IncidentStatus.NEW }
        DispatcherFilter.InProgress -> incidents.filter { it.status == IncidentStatus.IN_PROGRESS }
        DispatcherFilter.EnRoute -> incidents.filter { it.status == IncidentStatus.EN_ROUTE }
        DispatcherFilter.Resolved -> incidents.filter { it.status == IncidentStatus.RESOLVED }
    }
