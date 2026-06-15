package dev.koenv.roadassist.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.koenv.roadassist.app.theme.LocalRoadAssistColors
import dev.koenv.roadassist.app.theme.RoadAssistColors
import dev.koenv.roadassist.app.ui.components.AppDivider
import dev.koenv.roadassist.app.ui.components.EmptyState
import dev.koenv.roadassist.app.ui.components.IncidentListItem
import dev.koenv.roadassist.app.ui.foundation.LocalWindowSizeClass
import dev.koenv.roadassist.app.ui.foundation.WindowSizeClass
import dev.koenv.roadassist.app.ui.layouts.DispatcherLayout
import dev.koenv.roadassist.core.Incident
import dev.koenv.roadassist.core.IncidentStatus
import kotlinx.coroutines.delay

private enum class DispatcherFilter { All, New, InProgress, EnRoute, Resolved }

@Composable
fun DispatcherHomeScreen(
    viewModel: HomeViewModel,
    onLogout: () -> Unit,
    onIncidentClick: (Int) -> Unit,
    detailPanel: @Composable (Int) -> Unit,
) {
    val serverReachable by viewModel.serverReachable.collectAsState()
    val incidents by viewModel.incidents.collectAsState()
    val onLogoutClick: () -> Unit = { viewModel.logout(onLogout) }
    val windowSizeClass = LocalWindowSizeClass.current
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var selectedIncidentId by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000L)
            nowMillis = System.currentTimeMillis()
        }
    }
    var filter by remember { mutableStateOf(DispatcherFilter.All) }
    val filtered = filterIncidents(incidents, filter)

    DispatcherLayout(
        title = "All incidents",
        serverReachable = serverReachable,
        onLogout = onLogoutClick,
        headerTrailing = {
            IconButton(onClick = viewModel::refreshIncidents) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = LocalRoadAssistColors.current.mutedForeground)
            }
        },
    ) { padding ->
        if (windowSizeClass == WindowSizeClass.Compact) {
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                StatusFilterRow(
                    filter = filter,
                    onFilterChange = { filter = it },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
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
        } else {
            DispatcherSplitPane(
                filtered = filtered,
                filter = filter,
                onFilterChange = { filter = it },
                nowMillis = nowMillis,
                selectedIncidentId = selectedIncidentId,
                onSelectIncident = { selectedIncidentId = it },
                detailPanel = detailPanel,
                modifier = Modifier.fillMaxSize().padding(padding),
            )
        }
    }
}

@Composable
private fun DispatcherSplitPane(
    filtered: List<Incident>,
    filter: DispatcherFilter,
    onFilterChange: (DispatcherFilter) -> Unit,
    nowMillis: Long,
    selectedIncidentId: Int?,
    onSelectIncident: (Int) -> Unit,
    detailPanel: @Composable (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalRoadAssistColors.current
    Row(modifier = modifier) {
        Column(Modifier.width(300.dp).fillMaxHeight()) {
            StatusFilterRow(
                filter = filter,
                onFilterChange = onFilterChange,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                if (filtered.isEmpty()) {
                    item {
                        EmptyState("No incidents in queue", "New reports from road users will appear here.")
                    }
                } else {
                    items(filtered, key = { it.id }) { incident ->
                        IncidentListItem(
                            incident = incident,
                            nowMillis = nowMillis,
                            onClick = { onSelectIncident(incident.id) },
                            selected = incident.id == selectedIncidentId,
                            trailing = { UserIdBadge(incident.userId) },
                        )
                        AppDivider()
                    }
                }
            }
        }
        Box(Modifier.width(0.5.dp).fillMaxHeight().background(colors.border))
        Box(Modifier.weight(1f).fillMaxHeight()) {
            if (selectedIncidentId != null) {
                detailPanel(selectedIncidentId)
            } else {
                EmptyState("No incident selected", "Select an incident from the queue to view details.")
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
        modifier = modifier.fillMaxWidth().padding(vertical = 10.dp),
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

private fun filterIncidents(incidents: List<Incident>, filter: DispatcherFilter): List<Incident> =
    when (filter) {
        DispatcherFilter.All -> incidents
        DispatcherFilter.New -> incidents.filter { it.status == IncidentStatus.NEW }
        DispatcherFilter.InProgress -> incidents.filter { it.status == IncidentStatus.IN_PROGRESS }
        DispatcherFilter.EnRoute -> incidents.filter { it.status == IncidentStatus.EN_ROUTE }
        DispatcherFilter.Resolved -> incidents.filter { it.status == IncidentStatus.RESOLVED }
    }
