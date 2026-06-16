package dev.koenv.roadassist.app.ui.roaduser.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.koenv.roadassist.app.theme.LocalRoadAssistColors
import dev.koenv.roadassist.app.ui.components.AppDivider
import dev.koenv.roadassist.app.ui.components.EmptyState
import dev.koenv.roadassist.app.ui.components.IncidentListItem
import dev.koenv.roadassist.app.ui.components.PrimaryButton
import dev.koenv.roadassist.app.ui.foundation.LocalWindowSizeClass
import dev.koenv.roadassist.app.ui.foundation.WindowSizeClass
import dev.koenv.roadassist.app.ui.layouts.FabConfig
import dev.koenv.roadassist.app.ui.layouts.RoadUserLayout
import dev.koenv.roadassist.core.incident.Incident
import dev.koenv.roadassist.core.incident.IncidentStatus
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoadUserHomeScreen(
    viewModel: RoadUserHomeViewModel,
    onLogout: () -> Unit,
    onNewIncident: () -> Unit,
    onIncidentClick: (Int) -> Unit,
    detailPanel: @Composable (Int) -> Unit,
) {
    val serverReachable by viewModel.serverReachable.collectAsState()
    val incidents by viewModel.incidents.collectAsState()
    val incidentsLoading by viewModel.incidentsLoading.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val windowSizeClass = LocalWindowSizeClass.current
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var selectedIncidentId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        while (true) { delay(60_000L); nowMillis = System.currentTimeMillis() }
    }
    LaunchedEffect(selectedTab) { selectedIncidentId = null }

    RoadUserLayout(
        selectedTab = selectedTab,
        onTabChange = viewModel::selectTab,
        serverReachable = serverReachable,
        onLogout = { viewModel.logout(onLogout) },
        fab = if (selectedTab == RoadUserTab.Active && serverReachable) {
            FabConfig(Icons.Default.Add, "New incident", onNewIncident)
        } else {
            null
        },
        headerTrailing = {
            IconButton(onClick = viewModel::refreshIncidents) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = LocalRoadAssistColors.current.mutedForeground)
            }
        },
    ) { padding ->
        val filtered = filterByTab(incidents, selectedTab)
        if (windowSizeClass != WindowSizeClass.Expanded) {
            PullToRefreshBox(
                isRefreshing = incidentsLoading,
                onRefresh = viewModel::refreshIncidents,
                modifier = Modifier.fillMaxSize().padding(padding),
            ) {
                RoadUserIncidentList(
                    filtered = filtered,
                    incidentsLoading = incidentsLoading,
                    selectedTab = selectedTab,
                    serverReachable = serverReachable,
                    nowMillis = nowMillis,
                    onNewIncident = onNewIncident,
                    onIncidentClick = onIncidentClick,
                )
            }
        } else {
            RoadUserSplitPane(
                filtered = filtered,
                selectedTab = selectedTab,
                serverReachable = serverReachable,
                nowMillis = nowMillis,
                onNewIncident = onNewIncident,
                selectedIncidentId = selectedIncidentId,
                onSelectIncident = { selectedIncidentId = it },
                detailPanel = detailPanel,
                modifier = Modifier.fillMaxSize().padding(padding),
            )
        }
    }
}

@Composable
private fun RoadUserSplitPane(
    filtered: List<Incident>,
    selectedTab: RoadUserTab,
    serverReachable: Boolean,
    nowMillis: Long,
    onNewIncident: () -> Unit,
    selectedIncidentId: Int?,
    onSelectIncident: (Int) -> Unit,
    detailPanel: @Composable (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalRoadAssistColors.current
    Row(modifier = modifier) {
        Box(Modifier.width(360.dp).fillMaxHeight()) {
            RoadUserIncidentList(
                filtered = filtered,
                incidentsLoading = false,
                selectedTab = selectedTab,
                serverReachable = serverReachable,
                nowMillis = nowMillis,
                onNewIncident = onNewIncident,
                onIncidentClick = onSelectIncident,
                selectedIncidentId = selectedIncidentId,
            )
        }
        Box(Modifier.width(0.5.dp).fillMaxHeight().background(colors.border))
        Box(Modifier.weight(1f).fillMaxHeight()) {
            if (selectedIncidentId != null) {
                detailPanel(selectedIncidentId)
            } else {
                EmptyState("No incident selected", "Select an incident from the list to view details.")
            }
        }
    }
}

@Composable
private fun RoadUserIncidentList(
    filtered: List<Incident>,
    incidentsLoading: Boolean,
    selectedTab: RoadUserTab,
    serverReachable: Boolean,
    nowMillis: Long,
    onNewIncident: () -> Unit,
    onIncidentClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    selectedIncidentId: Int? = null,
) {
    if (filtered.isEmpty() && !incidentsLoading) {
        when (selectedTab) {
            RoadUserTab.Active -> EmptyState(
                title = "No active incidents",
                subtitle = "Your open reports will appear here.",
                action = if (serverReachable) {
                    { PrimaryButton(onClick = onNewIncident) { Text("Report an incident") } }
                } else {
                    null
                },
            )
            RoadUserTab.History -> EmptyState("No resolved incidents", "Past incidents will appear here once resolved.")
        }
    } else {
        LazyColumn(modifier = modifier) {
            items(filtered, key = { it.id }) { incident ->
                IncidentListItem(
                    incident = incident,
                    nowMillis = nowMillis,
                    onClick = { onIncidentClick(incident.id) },
                    selected = incident.id == selectedIncidentId,
                )
                AppDivider()
            }
        }
    }
}

private fun filterByTab(incidents: List<Incident>, tab: RoadUserTab): List<Incident> = when (tab) {
    RoadUserTab.Active -> incidents.filter { it.status != IncidentStatus.RESOLVED }.sortedByDescending { it.createdAt }
    RoadUserTab.History -> incidents.filter { it.status == IncidentStatus.RESOLVED }.sortedByDescending { it.updatedAt }
}
