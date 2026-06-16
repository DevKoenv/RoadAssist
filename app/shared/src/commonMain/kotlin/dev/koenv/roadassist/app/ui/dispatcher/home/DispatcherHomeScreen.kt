package dev.koenv.roadassist.app.ui.dispatcher.home

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import dev.koenv.roadassist.app.theme.LocalRoadAssistColors
import dev.koenv.roadassist.app.theme.RoadAssistColors
import dev.koenv.roadassist.app.ui.components.AppDivider
import dev.koenv.roadassist.app.ui.components.EmptyState
import dev.koenv.roadassist.app.ui.components.IncidentListItem
import dev.koenv.roadassist.app.ui.foundation.LocalWindowSizeClass
import dev.koenv.roadassist.app.ui.foundation.WindowSizeClass
import dev.koenv.roadassist.app.ui.layouts.DispatcherLayout
import dev.koenv.roadassist.core.incident.Incident
import kotlinx.coroutines.delay

@Composable
fun DispatcherHomeScreen(
    viewModel: DispatcherHomeViewModel,
    onLogout: () -> Unit,
    onIncidentClick: (Int) -> Unit,
    detailPanel: @Composable (Int) -> Unit,
) {
    val serverReachable by viewModel.serverReachable.collectAsState()
    val incidents by viewModel.incidents.collectAsState()
    val onLogoutClick: () -> Unit = { viewModel.logout(onLogout) }
    val windowSizeClass = LocalWindowSizeClass.current
    // Ticks every minute just to refresh "time ago" labels; fast enough for readability,
    // slow enough not to cause constant recomposition
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var selectedIncidentId by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000L)
            nowMillis = System.currentTimeMillis()
        }
    }

    // Filter state lives here in the screen, not in the ViewModel, so it survives auto-refresh
    var statusFilters by remember { mutableStateOf(emptySet<DispatcherStatusFilter>()) }
    var categoryFilters by remember { mutableStateOf(emptySet<DispatcherCategoryFilter>()) }
    val filtered = filterIncidents(incidents, statusFilters, categoryFilters)
    val filtersActive = statusFilters.isNotEmpty() || categoryFilters.isNotEmpty()

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
        if (windowSizeClass != WindowSizeClass.Expanded) {
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                FilterBar(
                    statusFilters = statusFilters,
                    onStatusFilterToggle = { f -> statusFilters = if (f in statusFilters) statusFilters - f else statusFilters + f },
                    categoryFilters = categoryFilters,
                    onCategoryFilterToggle = { f -> categoryFilters = if (f in categoryFilters) categoryFilters - f else categoryFilters + f },
                    filtersActive = filtersActive,
                    onClearFilters = {
                        statusFilters = emptySet()
                        categoryFilters = emptySet()
                    },
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
                statusFilters = statusFilters,
                onStatusFilterToggle = { f -> statusFilters = if (f in statusFilters) statusFilters - f else statusFilters + f },
                categoryFilters = categoryFilters,
                onCategoryFilterToggle = { f -> categoryFilters = if (f in categoryFilters) categoryFilters - f else categoryFilters + f },
                filtersActive = filtersActive,
                onClearFilters = {
                    statusFilters = emptySet()
                    categoryFilters = emptySet()
                },
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
    statusFilters: Set<DispatcherStatusFilter>,
    onStatusFilterToggle: (DispatcherStatusFilter) -> Unit,
    categoryFilters: Set<DispatcherCategoryFilter>,
    onCategoryFilterToggle: (DispatcherCategoryFilter) -> Unit,
    filtersActive: Boolean,
    onClearFilters: () -> Unit,
    nowMillis: Long,
    selectedIncidentId: Int?,
    onSelectIncident: (Int) -> Unit,
    detailPanel: @Composable (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalRoadAssistColors.current
    Row(modifier = modifier) {
        // incident list panel
        Column(Modifier.width(360.dp).fillMaxHeight()) {
            FilterBar(
                statusFilters = statusFilters,
                onStatusFilterToggle = onStatusFilterToggle,
                categoryFilters = categoryFilters,
                onCategoryFilterToggle = onCategoryFilterToggle,
                filtersActive = filtersActive,
                onClearFilters = onClearFilters,
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
        // list/detail divider
        Box(Modifier.width(0.5.dp).fillMaxHeight().background(colors.border))
        // detail panel area
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
private fun FilterBar(
    statusFilters: Set<DispatcherStatusFilter>,
    onStatusFilterToggle: (DispatcherStatusFilter) -> Unit,
    categoryFilters: Set<DispatcherCategoryFilter>,
    onCategoryFilterToggle: (DispatcherCategoryFilter) -> Unit,
    filtersActive: Boolean,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val extColors = LocalRoadAssistColors.current
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = Icons.Default.FilterList,
            contentDescription = null,
            tint = extColors.mutedForeground,
            modifier = Modifier.size(18.dp),
        )
        MultiSelectDropdown(
            label = if (statusFilters.isEmpty()) "Status" else "Status · ${statusFilters.size}",
            items = DispatcherStatusFilter.entries,
            selected = statusFilters,
            itemLabel = { it.displayName },
            onToggle = onStatusFilterToggle,
        )
        MultiSelectDropdown(
            label = if (categoryFilters.isEmpty()) "Category" else "Category · ${categoryFilters.size}",
            items = DispatcherCategoryFilter.entries,
            selected = categoryFilters,
            itemLabel = { it.displayName },
            onToggle = onCategoryFilterToggle,
        )
        if (filtersActive) {
            TextButton(onClick = onClearFilters) {
                Text(
                    text = "Clear",
                    style = MaterialTheme.typography.labelMedium,
                    color = extColors.mutedForeground,
                )
            }
        }
    }
    AppDivider()
}

@Composable
private fun <T> MultiSelectDropdown(
    label: String,
    items: List<T>,
    selected: Set<T>,
    itemLabel: (T) -> String,
    onToggle: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    val extColors = LocalRoadAssistColors.current
    var expanded by remember { mutableStateOf(false) }
    val isActive = selected.isNotEmpty()

    Box(modifier = modifier) {
        Surface(
            onClick = { expanded = true },
            shape = MaterialTheme.shapes.small,
            color = if (isActive) RoadAssistColors.Accent else extColors.muted,
            tonalElevation = 0.dp,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (isActive) MaterialTheme.colorScheme.primary else extColors.mutedForeground,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            items.forEach { item ->
                val isSelected = item in selected
                DropdownMenuItem(
                    text = {
                        Text(
                            text = itemLabel(item),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    } else {
                        null
                    },
                    onClick = { onToggle(item) },
                )
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
