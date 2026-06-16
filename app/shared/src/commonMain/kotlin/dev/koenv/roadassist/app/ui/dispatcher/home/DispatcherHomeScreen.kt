package dev.koenv.roadassist.app.ui.dispatcher.home

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
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
import dev.koenv.roadassist.core.incident.Incident
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    var statusFilter by remember { mutableStateOf(DispatcherStatusFilter.All) }
    var categoryFilter by remember { mutableStateOf(DispatcherCategoryFilter.All) }
    val filtered = filterIncidents(incidents, statusFilter, categoryFilter)
    val filtersActive = statusFilter != DispatcherStatusFilter.All || categoryFilter != DispatcherCategoryFilter.All

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
                FilterSection(
                    statusFilter = statusFilter,
                    onStatusFilterChange = { statusFilter = it },
                    categoryFilter = categoryFilter,
                    onCategoryFilterChange = { categoryFilter = it },
                    filtersActive = filtersActive,
                    onClearFilters = {
                        statusFilter = DispatcherStatusFilter.All
                        categoryFilter = DispatcherCategoryFilter.All
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
                statusFilter = statusFilter,
                onStatusFilterChange = { statusFilter = it },
                categoryFilter = categoryFilter,
                onCategoryFilterChange = { categoryFilter = it },
                filtersActive = filtersActive,
                onClearFilters = {
                    statusFilter = DispatcherStatusFilter.All
                    categoryFilter = DispatcherCategoryFilter.All
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
    statusFilter: DispatcherStatusFilter,
    onStatusFilterChange: (DispatcherStatusFilter) -> Unit,
    categoryFilter: DispatcherCategoryFilter,
    onCategoryFilterChange: (DispatcherCategoryFilter) -> Unit,
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
            FilterSection(
                statusFilter = statusFilter,
                onStatusFilterChange = onStatusFilterChange,
                categoryFilter = categoryFilter,
                onCategoryFilterChange = onCategoryFilterChange,
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

// Groups both filter chip rows and the clear button into one composable so call sites stay simple
@Composable
private fun FilterSection(
    statusFilter: DispatcherStatusFilter,
    onStatusFilterChange: (DispatcherStatusFilter) -> Unit,
    categoryFilter: DispatcherCategoryFilter,
    onCategoryFilterChange: (DispatcherCategoryFilter) -> Unit,
    filtersActive: Boolean,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val extColors = LocalRoadAssistColors.current
    Column(modifier = modifier) {
        StatusFilterRow(
            filter = statusFilter,
            onFilterChange = onStatusFilterChange,
        )
        CategoryFilterRow(
            filter = categoryFilter,
            onFilterChange = onCategoryFilterChange,
        )
        if (filtersActive) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onClearFilters) {
                    Text(
                        text = "Clear filters",
                        style = MaterialTheme.typography.labelMedium,
                        color = extColors.mutedForeground,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusFilterRow(
    filter: DispatcherStatusFilter,
    onFilterChange: (DispatcherStatusFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    val extColors = LocalRoadAssistColors.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val scrollAmount = 140

    val bg = MaterialTheme.colorScheme.background
    Box(modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp).clipToBounds()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 8.dp).horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DispatcherStatusFilter.entries.forEach { f ->
                FilterChip(
                    selected = filter == f,
                    onClick = { onFilterChange(f) },
                    label = {
                        Text(
                            text = when (f) {
                                DispatcherStatusFilter.All -> "All"
                                DispatcherStatusFilter.New -> "New"
                                DispatcherStatusFilter.InProgress -> "In progress"
                                DispatcherStatusFilter.EnRoute -> "En route"
                                DispatcherStatusFilter.Resolved -> "Resolved"
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
        if (scrollState.canScrollBackward) {
            IconButton(
                onClick = { scope.launch { scrollState.animateScrollTo((scrollState.value - scrollAmount).coerceAtLeast(0)) } },
                modifier = Modifier.align(Alignment.CenterStart).size(28.dp).background(bg),
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Scroll left", modifier = Modifier.size(18.dp), tint = extColors.mutedForeground)
            }
        }
        if (scrollState.canScrollForward) {
            IconButton(
                onClick = { scope.launch { scrollState.animateScrollTo((scrollState.value + scrollAmount).coerceAtMost(scrollState.maxValue)) } },
                modifier = Modifier.align(Alignment.CenterEnd).size(28.dp).background(bg),
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Scroll right", modifier = Modifier.size(18.dp), tint = extColors.mutedForeground)
            }
        }
    }
}

@Composable
private fun CategoryFilterRow(
    filter: DispatcherCategoryFilter,
    onFilterChange: (DispatcherCategoryFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    val extColors = LocalRoadAssistColors.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val scrollAmount = 140

    val bg = MaterialTheme.colorScheme.background
    Box(modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp).clipToBounds()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 8.dp).horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DispatcherCategoryFilter.entries.forEach { f ->
                FilterChip(
                    selected = filter == f,
                    onClick = { onFilterChange(f) },
                    label = {
                        Text(
                            text = when (f) {
                                DispatcherCategoryFilter.All -> "All"
                                DispatcherCategoryFilter.Breakdown -> "Breakdown"
                                DispatcherCategoryFilter.Accident -> "Accident"
                                DispatcherCategoryFilter.Obstruction -> "Obstruction"
                                DispatcherCategoryFilter.Other -> "Other"
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
        if (scrollState.canScrollBackward) {
            IconButton(
                onClick = { scope.launch { scrollState.animateScrollTo((scrollState.value - scrollAmount).coerceAtLeast(0)) } },
                modifier = Modifier.align(Alignment.CenterStart).size(28.dp).background(bg),
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Scroll left", modifier = Modifier.size(18.dp), tint = extColors.mutedForeground)
            }
        }
        if (scrollState.canScrollForward) {
            IconButton(
                onClick = { scope.launch { scrollState.animateScrollTo((scrollState.value + scrollAmount).coerceAtMost(scrollState.maxValue)) } },
                modifier = Modifier.align(Alignment.CenterEnd).size(28.dp).background(bg),
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Scroll right", modifier = Modifier.size(18.dp), tint = extColors.mutedForeground)
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
