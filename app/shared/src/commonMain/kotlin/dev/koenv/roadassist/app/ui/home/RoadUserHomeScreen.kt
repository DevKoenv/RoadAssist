package dev.koenv.roadassist.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import dev.koenv.roadassist.app.ui.components.LogoutTextButton
import dev.koenv.roadassist.app.ui.components.MobileAppBar
import dev.koenv.roadassist.app.ui.components.NavItemContent
import dev.koenv.roadassist.app.ui.components.NavRailItem
import dev.koenv.roadassist.app.ui.components.PrimaryButton

internal enum class RoadUserTab { Active, History }

@Composable
fun RoadUserHomeScreen(
    viewModel: HomeViewModel,
    onLogout: () -> Unit,
    onNewIncident: () -> Unit,
) {
    val serverReachable by viewModel.serverReachable.collectAsState()
    val onLogoutClick: () -> Unit = { viewModel.logout(onLogout) }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (maxWidth >= 700.dp) {
            RoadUserDesktopLayout(onLogout = onLogoutClick, serverReachable = serverReachable, onNewIncident = onNewIncident)
        } else {
            RoadUserMobileLayout(onLogout = onLogoutClick, serverReachable = serverReachable, onNewIncident = onNewIncident)
        }
    }
}

@Composable
private fun RoadUserMobileLayout(onLogout: () -> Unit, serverReachable: Boolean, onNewIncident: () -> Unit) {
    var selectedTab by remember { mutableStateOf(RoadUserTab.Active) }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { RoadUserBottomBar(selectedTab = selectedTab, onTabChange = { selectedTab = it }) },
        floatingActionButton = {
            if (selectedTab == RoadUserTab.Active) {
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
            when (selectedTab) {
                RoadUserTab.Active -> EmptyState("No active incidents", "Your open reports will appear here.")
                RoadUserTab.History -> EmptyState("No resolved incidents", "Past incidents will appear here once resolved.")
            }
        }
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
                icon = { Icon(Icons.Default.List, contentDescription = null, tint = if (selectedTab == RoadUserTab.Active) MaterialTheme.colorScheme.primary else LocalRoadAssistColors.current.mutedForeground) },
                label = "Active",
            )
            BottomNavItem(
                selected = selectedTab == RoadUserTab.History,
                onClick = { onTabChange(RoadUserTab.History) },
                icon = { Icon(Icons.Default.History, contentDescription = null, tint = if (selectedTab == RoadUserTab.History) MaterialTheme.colorScheme.primary else LocalRoadAssistColors.current.mutedForeground) },
                label = "History",
            )
        }
    }
}

@Composable
private fun RoadUserDesktopLayout(onLogout: () -> Unit, serverReachable: Boolean, onNewIncident: () -> Unit) {
    var selectedTab by remember { mutableStateOf(RoadUserTab.Active) }
    Row(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        RoadUserNavRail(selectedTab = selectedTab, onTabChange = { selectedTab = it }, onLogout = onLogout)
        Box(modifier = Modifier.width(0.5.dp).fillMaxSize().background(LocalRoadAssistColors.current.border))
        Column(modifier = Modifier.weight(1f).fillMaxSize()) {
            ConnectivityBanner(visible = !serverReachable)
            DesktopPageHeader(
                title = if (selectedTab == RoadUserTab.Active) "Active incidents" else "History",
                trailing = {
                    if (selectedTab == RoadUserTab.Active) {
                        PrimaryButton(onClick = onNewIncident) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("New incident", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                },
            )
            AppDivider()
            when (selectedTab) {
                RoadUserTab.Active -> EmptyState("No active incidents", "Your open reports will appear here.")
                RoadUserTab.History -> EmptyState("No resolved incidents", "Past incidents will appear here once resolved.")
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
            icon = { Icon(Icons.Default.List, contentDescription = null, tint = if (selectedTab == RoadUserTab.Active) MaterialTheme.colorScheme.primary else LocalRoadAssistColors.current.mutedForeground) },
            label = "Active",
        )
        NavRailItem(
            selected = selectedTab == RoadUserTab.History,
            onClick = { onTabChange(RoadUserTab.History) },
            icon = { Icon(Icons.Default.History, contentDescription = null, tint = if (selectedTab == RoadUserTab.History) MaterialTheme.colorScheme.primary else LocalRoadAssistColors.current.mutedForeground) },
            label = "History",
        )
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
