package dev.koenv.roadassist.app.ui.home

import androidx.compose.foundation.background
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import dev.koenv.roadassist.app.theme.LocalRoadAssistColors
import dev.koenv.roadassist.app.theme.RoadAssistColors
import dev.koenv.roadassist.app.ui.components.ConnectivityBanner
import dev.koenv.roadassist.app.ui.login.RoadAssistAppIcon

private enum class RoadUserTab { Active, History }

@Composable
fun RoadUserHomeScreen(
    viewModel: HomeViewModel,
    onLogout: () -> Unit,
) {
    val serverReachable by viewModel.serverReachable.collectAsState()
    val onLogoutClick: () -> Unit = { viewModel.logout(onLogout) }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (maxWidth >= 700.dp) {
            RoadUserDesktopLayout(onLogout = onLogoutClick, serverReachable = serverReachable)
        } else {
            RoadUserMobileLayout(onLogout = onLogoutClick, serverReachable = serverReachable)
        }
    }
}

@Composable
private fun RoadUserMobileLayout(onLogout: () -> Unit, serverReachable: Boolean) {
    var selectedTab by remember { mutableStateOf(RoadUserTab.Active) }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { RoadUserBottomBar(selectedTab = selectedTab, onTabChange = { selectedTab = it }) },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            ConnectivityBanner(visible = !serverReachable)
            MobileScreenHeader(title = if (selectedTab == RoadUserTab.Active) "Active" else "History", onLogout = onLogout)
            HorizontalDivider(color = LocalRoadAssistColors.current.border, thickness = 0.5.dp)
            when (selectedTab) {
                RoadUserTab.Active -> ActiveIncidentsEmptyState()
                RoadUserTab.History -> HistoryEmptyState()
            }
        }
    }
}

@Composable
private fun RoadUserBottomBar(selectedTab: RoadUserTab, onTabChange: (RoadUserTab) -> Unit) {
    val navColors = NavigationBarItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.primary,
        selectedTextColor = MaterialTheme.colorScheme.primary,
        indicatorColor = RoadAssistColors.Accent,
        unselectedIconColor = LocalRoadAssistColors.current.mutedForeground,
        unselectedTextColor = LocalRoadAssistColors.current.mutedForeground,
    )
    Column {
        HorizontalDivider(color = LocalRoadAssistColors.current.border, thickness = 0.5.dp)
        NavigationBar(containerColor = MaterialTheme.colorScheme.background, tonalElevation = 0.dp) {
            NavigationBarItem(
                selected = selectedTab == RoadUserTab.Active,
                onClick = { onTabChange(RoadUserTab.Active) },
                icon = { ListIcon(selected = selectedTab == RoadUserTab.Active) },
                label = { Text("Active", style = MaterialTheme.typography.labelMedium) },
                colors = navColors,
            )
            NavigationBarItem(
                selected = selectedTab == RoadUserTab.History,
                onClick = { onTabChange(RoadUserTab.History) },
                icon = { HistoryIcon(selected = selectedTab == RoadUserTab.History) },
                label = { Text("History", style = MaterialTheme.typography.labelMedium) },
                colors = navColors,
            )
        }
    }
}

@Composable
private fun MobileScreenHeader(title: String, onLogout: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
        LogoutButton(onLogout = onLogout)
    }
}

@Composable
private fun RoadUserDesktopLayout(onLogout: () -> Unit, serverReachable: Boolean) {
    var selectedTab by remember { mutableStateOf(RoadUserTab.Active) }
    Row(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        RoadUserNavRail(selectedTab = selectedTab, onTabChange = { selectedTab = it }, onLogout = onLogout)
        Box(modifier = Modifier.width(0.5.dp).fillMaxSize().background(LocalRoadAssistColors.current.border))
        Column(modifier = Modifier.weight(1f).fillMaxSize()) {
            ConnectivityBanner(visible = !serverReachable)
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
                Text(
                    text = if (selectedTab == RoadUserTab.Active) "Active incidents" else "History",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
            HorizontalDivider(color = LocalRoadAssistColors.current.border, thickness = 0.5.dp)
            when (selectedTab) {
                RoadUserTab.Active -> ActiveIncidentsEmptyState()
                RoadUserTab.History -> HistoryEmptyState()
            }
        }
    }
}

@Composable
private fun RoadUserNavRail(
    selectedTab: RoadUserTab,
    onTabChange: (RoadUserTab) -> Unit,
    onLogout: () -> Unit,
) {
    val railColors = NavigationRailItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.primary,
        selectedTextColor = MaterialTheme.colorScheme.primary,
        indicatorColor = RoadAssistColors.Accent,
        unselectedIconColor = LocalRoadAssistColors.current.mutedForeground,
        unselectedTextColor = LocalRoadAssistColors.current.mutedForeground,
    )
    NavigationRail(containerColor = MaterialTheme.colorScheme.background, contentColor = LocalRoadAssistColors.current.mutedForeground) {
        Spacer(Modifier.height(8.dp))
        Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) { RoadAssistAppIcon(size = 40.dp) }
        Spacer(Modifier.height(16.dp))
        NavigationRailItem(selected = selectedTab == RoadUserTab.Active, onClick = { onTabChange(RoadUserTab.Active) }, icon = { ListIcon(selected = selectedTab == RoadUserTab.Active) }, label = { Text("Active", style = MaterialTheme.typography.labelMedium) }, colors = railColors)
        NavigationRailItem(selected = selectedTab == RoadUserTab.History, onClick = { onTabChange(RoadUserTab.History) }, icon = { HistoryIcon(selected = selectedTab == RoadUserTab.History) }, label = { Text("History", style = MaterialTheme.typography.labelMedium) }, colors = railColors)
        Spacer(Modifier.weight(1f))
        NavigationRailItem(selected = false, onClick = onLogout, icon = { LogoutIconSmall() }, label = null, colors = NavigationRailItemDefaults.colors(unselectedIconColor = LocalRoadAssistColors.current.mutedForeground))
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ActiveIncidentsEmptyState() {
    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "No active incidents",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Your open reports will appear here.",
                style = MaterialTheme.typography.bodySmall,
                color = LocalRoadAssistColors.current.mutedForeground,
            )
        }
    }
}

@Composable
private fun HistoryEmptyState() {
    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "No resolved incidents",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Past incidents will appear here once resolved.",
                style = MaterialTheme.typography.bodySmall,
                color = LocalRoadAssistColors.current.mutedForeground,
            )
        }
    }
}

@Composable
private fun LogoutButton(onLogout: () -> Unit) {
    TextButton(onClick = onLogout) {
        Text(
            text = "Log out",
            style = MaterialTheme.typography.bodySmall,
            color = LocalRoadAssistColors.current.mutedForeground,
        )
    }
}

@Composable
private fun ListIcon(selected: Boolean) {
    val color = if (selected) MaterialTheme.colorScheme.primary else LocalRoadAssistColors.current.mutedForeground
    Box(
        modifier = Modifier
            .size(22.dp)
            .drawBehind {
                val w = size.width
                val h = size.height
                val stroke = w * 0.1f
                val gap = h * 0.22f
                for (i in 0..2) {
                    val y = h * 0.2f + i * gap
                    drawLine(
                        color = color,
                        start = Offset(w * 0.1f, y),
                        end = Offset(w * 0.45f, y),
                        strokeWidth = stroke,
                        cap = StrokeCap.Round,
                    )
                    drawLine(
                        color = color,
                        start = Offset(w * 0.55f, y),
                        end = Offset(w * 0.9f, y),
                        strokeWidth = stroke,
                        cap = StrokeCap.Round,
                    )
                }
            },
    )
}

@Composable
private fun HistoryIcon(selected: Boolean) {
    val color = if (selected) MaterialTheme.colorScheme.primary else LocalRoadAssistColors.current.mutedForeground
    Box(
        modifier = Modifier
            .size(22.dp)
            .drawBehind {
                val r = size.width * 0.42f
                val cx = size.width / 2f
                val cy = size.height / 2f
                val stroke = size.width * 0.1f
                drawCircle(
                    color = color,
                    radius = r,
                    center = Offset(cx, cy),
                    style = Stroke(width = stroke),
                )
                drawLine(
                    color = color,
                    start = Offset(cx, cy),
                    end = Offset(cx, cy - r * 0.6f),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = color,
                    start = Offset(cx, cy),
                    end = Offset(cx + r * 0.4f, cy),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round,
                )
            },
    )
}

@Composable
private fun LogoutIconSmall() {
    val color = LocalRoadAssistColors.current.mutedForeground
    Box(
        modifier = Modifier
            .size(22.dp)
            .drawBehind {
                val w = size.width
                val h = size.height
                val stroke = w * 0.1f
                val arrowX = w * 0.58f
                drawLine(color = color, start = Offset(arrowX, h * 0.5f), end = Offset(w * 0.92f, h * 0.5f), strokeWidth = stroke, cap = StrokeCap.Round)
                drawLine(color = color, start = Offset(w * 0.72f, h * 0.32f), end = Offset(w * 0.92f, h * 0.5f), strokeWidth = stroke, cap = StrokeCap.Round)
                drawLine(color = color, start = Offset(w * 0.72f, h * 0.68f), end = Offset(w * 0.92f, h * 0.5f), strokeWidth = stroke, cap = StrokeCap.Round)
                drawLine(color = color, start = Offset(arrowX, h * 0.5f), end = Offset(arrowX, h * 0.2f), strokeWidth = stroke, cap = StrokeCap.Round)
                drawLine(color = color, start = Offset(arrowX, h * 0.2f), end = Offset(w * 0.1f, h * 0.2f), strokeWidth = stroke, cap = StrokeCap.Round)
                drawLine(color = color, start = Offset(w * 0.1f, h * 0.2f), end = Offset(w * 0.1f, h * 0.8f), strokeWidth = stroke, cap = StrokeCap.Round)
                drawLine(color = color, start = Offset(w * 0.1f, h * 0.8f), end = Offset(arrowX, h * 0.8f), strokeWidth = stroke, cap = StrokeCap.Round)
                drawLine(color = color, start = Offset(arrowX, h * 0.8f), end = Offset(arrowX, h * 0.5f), strokeWidth = stroke, cap = StrokeCap.Round)
            },
    )
}
