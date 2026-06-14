package dev.koenv.roadassist.app.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.koenv.roadassist.app.theme.LocalRoadAssistColors
import dev.koenv.roadassist.app.ui.components.AppDivider
import dev.koenv.roadassist.app.ui.components.AppNavRail
import dev.koenv.roadassist.app.ui.components.ConnectivityBanner
import dev.koenv.roadassist.app.ui.components.DesktopPageHeader
import dev.koenv.roadassist.app.ui.components.EmptyState
import dev.koenv.roadassist.app.ui.components.IncidentListItem
import dev.koenv.roadassist.app.ui.components.LogoutTextButton
import dev.koenv.roadassist.app.ui.components.MobileAppBar
import dev.koenv.roadassist.app.ui.components.NavItemContent
import dev.koenv.roadassist.app.ui.components.NavRailItem
import dev.koenv.roadassist.app.ui.components.PrimaryButton
import dev.koenv.roadassist.core.Incident
import dev.koenv.roadassist.core.IncidentStatus
import kotlinx.coroutines.delay

enum class RoadUserTab { Active, History }

@Composable
fun RoadUserHomeScreen(
    viewModel: HomeViewModel,
    isDesktop: Boolean,
    selectedTab: RoadUserTab,
    onTabChange: (RoadUserTab) -> Unit,
    onLogout: () -> Unit,
    onNewIncident: () -> Unit,
    onIncidentClick: (Int) -> Unit,
) {
    val serverReachable by viewModel.serverReachable.collectAsState()
    val incidents by viewModel.incidents.collectAsState()
    val incidentsLoading by viewModel.incidentsLoading.collectAsState()
    val onLogoutClick: () -> Unit = { viewModel.logout(onLogout) }
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000L)
            nowMillis = System.currentTimeMillis()
        }
    }

    if (isDesktop) {
        RoadUserDesktopLayout(
            incidents = incidents,
            selectedTab = selectedTab,
            serverReachable = serverReachable,
            nowMillis = nowMillis,
            onRefresh = viewModel::refreshIncidents,
            onNewIncident = onNewIncident,
            onIncidentClick = onIncidentClick,
        )
    } else {
        RoadUserMobileLayout(
            incidents = incidents,
            selectedTab = selectedTab,
            incidentsLoading = incidentsLoading,
            serverReachable = serverReachable,
            nowMillis = nowMillis,
            onLogout = onLogoutClick,
            onTabChange = onTabChange,
            onNewIncident = onNewIncident,
            onIncidentClick = onIncidentClick,
            onRefresh = viewModel::refreshIncidents,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoadUserMobileLayout(
    incidents: List<Incident>,
    selectedTab: RoadUserTab,
    incidentsLoading: Boolean,
    serverReachable: Boolean,
    nowMillis: Long,
    onLogout: () -> Unit,
    onTabChange: (RoadUserTab) -> Unit,
    onNewIncident: () -> Unit,
    onIncidentClick: (Int) -> Unit,
    onRefresh: () -> Unit,
) {
    val filtered = filterByTab(incidents, selectedTab)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { RoadUserBottomBar(selectedTab = selectedTab, onTabChange = onTabChange) },
        floatingActionButton = {
            if (selectedTab == RoadUserTab.Active && serverReachable) {
                FloatingActionButton(
                    onClick = onNewIncident,
                    containerColor = MaterialTheme.colorScheme.primary,
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New incident", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            ConnectivityBanner(visible = !serverReachable)
            MobileAppBar(
                title = if (selectedTab == RoadUserTab.Active) "Active" else "History",
                trailing = { LogoutTextButton(onClick = onLogout) },
            )
            AppDivider()
            PullToRefreshBox(
                isRefreshing = incidentsLoading,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize(),
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
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(filtered, key = { it.id }) { incident ->
                            IncidentListItem(
                                incident = incident,
                                nowMillis = nowMillis,
                                onClick = { onIncidentClick(incident.id) },
                            )
                            AppDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoadUserDesktopLayout(
    incidents: List<Incident>,
    selectedTab: RoadUserTab,
    serverReachable: Boolean,
    nowMillis: Long,
    onRefresh: () -> Unit,
    onNewIncident: () -> Unit,
    onIncidentClick: (Int) -> Unit,
) {
    val filtered = filterByTab(incidents, selectedTab)

    Column(modifier = Modifier.fillMaxSize()) {
        ConnectivityBanner(visible = !serverReachable)
        DesktopPageHeader(
            title = if (selectedTab == RoadUserTab.Active) "Active incidents" else "History",
            trailing = {
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = LocalRoadAssistColors.current.mutedForeground)
                }
                if (selectedTab == RoadUserTab.Active && serverReachable) {
                    PrimaryButton(onClick = onNewIncident) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("New incident", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            },
        )
        AppDivider()
        if (filtered.isEmpty()) {
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
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filtered, key = { it.id }) { incident ->
                    IncidentListItem(
                        incident = incident,
                        nowMillis = nowMillis,
                        onClick = { onIncidentClick(incident.id) },
                    )
                    AppDivider()
                }
            }
        }
    }
}

@Composable
internal fun RoadUserNavRail(
    selectedTab: RoadUserTab,
    onTabChange: (RoadUserTab) -> Unit,
    onLogout: () -> Unit,
) {
    AppNavRail(onLogout = onLogout) {
        NavRailItem(
            selected = selectedTab == RoadUserTab.Active,
            onClick = { onTabChange(RoadUserTab.Active) },
            icon = {
                Icon(
                    Icons.AutoMirrored.Filled.List,
                    contentDescription = null,
                    tint = if (selectedTab == RoadUserTab.Active) MaterialTheme.colorScheme.primary else LocalRoadAssistColors.current.mutedForeground,
                )
            },
            label = "Active",
        )
        NavRailItem(
            selected = selectedTab == RoadUserTab.History,
            onClick = { onTabChange(RoadUserTab.History) },
            icon = {
                Icon(
                    Icons.Default.History,
                    contentDescription = null,
                    tint = if (selectedTab == RoadUserTab.History) MaterialTheme.colorScheme.primary else LocalRoadAssistColors.current.mutedForeground,
                )
            },
            label = "History",
        )
    }
}

@Composable
private fun RoadUserBottomBar(selectedTab: RoadUserTab, onTabChange: (RoadUserTab) -> Unit) {
    Column {
        AppDivider()
        NavigationBar(containerColor = MaterialTheme.colorScheme.background, tonalElevation = 0.dp) {
            BottomNavItem(
                selected = selectedTab == RoadUserTab.Active,
                onClick = { onTabChange(RoadUserTab.Active) },
                icon = {
                    Icon(
                        Icons.AutoMirrored.Filled.List,
                        contentDescription = null,
                        tint = if (selectedTab == RoadUserTab.Active) MaterialTheme.colorScheme.primary else LocalRoadAssistColors.current.mutedForeground,
                    )
                },
                label = "Active",
            )
            BottomNavItem(
                selected = selectedTab == RoadUserTab.History,
                onClick = { onTabChange(RoadUserTab.History) },
                icon = {
                    Icon(
                        Icons.Default.History,
                        contentDescription = null,
                        tint = if (selectedTab == RoadUserTab.History) MaterialTheme.colorScheme.primary else LocalRoadAssistColors.current.mutedForeground,
                    )
                },
                label = "History",
            )
        }
    }
}

@Composable
private fun RowScope.BottomNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    label: String,
) = NavItemContent(
    selected = selected,
    onClick = onClick,
    icon = icon,
    label = label,
    modifier = Modifier.weight(1f).padding(vertical = 8.dp),
)

private fun filterByTab(incidents: List<Incident>, tab: RoadUserTab): List<Incident> = when (tab) {
    RoadUserTab.Active -> incidents.filter { it.status != IncidentStatus.RESOLVED }.sortedByDescending { it.createdAt }
    RoadUserTab.History -> incidents.filter { it.status == IncidentStatus.RESOLVED }.sortedByDescending { it.updatedAt }
}
