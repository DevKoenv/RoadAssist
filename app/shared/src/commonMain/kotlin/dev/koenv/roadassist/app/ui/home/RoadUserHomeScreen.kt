package dev.koenv.roadassist.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationRail
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
import androidx.compose.ui.unit.dp
import dev.koenv.roadassist.app.theme.LocalRoadAssistColors
import dev.koenv.roadassist.app.ui.components.ConnectivityBanner
import dev.koenv.roadassist.app.ui.login.RoadAssistAppIcon

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
                    ReportIcon(color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        },
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
    Column {
        HorizontalDivider(color = LocalRoadAssistColors.current.border, thickness = 0.5.dp)
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
private fun RoadUserDesktopLayout(onLogout: () -> Unit, serverReachable: Boolean, onNewIncident: () -> Unit) {
    var selectedTab by remember { mutableStateOf(RoadUserTab.Active) }
    Row(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        RoadUserNavRail(selectedTab = selectedTab, onTabChange = { selectedTab = it }, onLogout = onLogout)
        Box(modifier = Modifier.width(0.5.dp).fillMaxSize().background(LocalRoadAssistColors.current.border))
        Column(modifier = Modifier.weight(1f).fillMaxSize()) {
            ConnectivityBanner(visible = !serverReachable)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (selectedTab == RoadUserTab.Active) "Active incidents" else "History",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                if (selectedTab == RoadUserTab.Active) {
                    Button(
                        onClick = onNewIncident,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    ) {
                        PlusIcon(color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(Modifier.width(6.dp))
                        Text("New incident", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.bodyMedium)
                    }
                }
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
internal fun RoadUserNavRail(
    selectedTab: RoadUserTab,
    onTabChange: (RoadUserTab) -> Unit,
    onLogout: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(80.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(8.dp))
        Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) { RoadAssistAppIcon(size = 40.dp) }
        Spacer(Modifier.height(16.dp))
        RailNavItem(
            selected = selectedTab == RoadUserTab.Active,
            onClick = { onTabChange(RoadUserTab.Active) },
            icon = { Icon(Icons.Default.List, contentDescription = null, tint = if (selectedTab == RoadUserTab.Active) MaterialTheme.colorScheme.primary else LocalRoadAssistColors.current.mutedForeground) },
            label = "Active",
        )
        RailNavItem(
            selected = selectedTab == RoadUserTab.History,
            onClick = { onTabChange(RoadUserTab.History) },
            icon = { Icon(Icons.Default.History, contentDescription = null, tint = if (selectedTab == RoadUserTab.History) MaterialTheme.colorScheme.primary else LocalRoadAssistColors.current.mutedForeground) },
            label = "History",
        )
        Spacer(Modifier.weight(1f))
        RailNavItem(
            selected = false,
            onClick = onLogout,
            icon = { Icon(Icons.AutoMirrored.Default.Logout, contentDescription = "Log out", tint = LocalRoadAssistColors.current.mutedForeground) },
            label = "",
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun RailNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    label: String,
) {
    val accent = LocalRoadAssistColors.current.accent
    val primary = MaterialTheme.colorScheme.primary
    val muted = LocalRoadAssistColors.current.mutedForeground
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .then(if (selected) Modifier.background(accent, RoundedCornerShape(10.dp)) else Modifier),
            contentAlignment = Alignment.Center,
        ) { icon() }
        Spacer(Modifier.height(2.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = if (selected) primary else muted)
    }
}

@Composable
private fun RowScope.BottomNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    label: String,
) {
    val accent = LocalRoadAssistColors.current.accent
    val primary = MaterialTheme.colorScheme.primary
    val muted = LocalRoadAssistColors.current.mutedForeground
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .then(if (selected) Modifier.background(accent, RoundedCornerShape(10.dp)) else Modifier),
            contentAlignment = Alignment.Center,
        ) { icon() }
        Spacer(Modifier.height(2.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = if (selected) primary else muted)
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
private fun PlusIcon(color: Color) {
    Box(
        modifier = Modifier
            .size(18.dp)
            .drawBehind {
                val cx = size.width / 2f; val cy = size.height / 2f
                val half = size.width * 0.38f; val s = size.width * 0.12f
                drawLine(color, Offset(cx - half, cy), Offset(cx + half, cy), s, StrokeCap.Round)
                drawLine(color, Offset(cx, cy - half), Offset(cx, cy + half), s, StrokeCap.Round)
            },
    )
}

@Composable
private fun ReportIcon(color: Color = MaterialTheme.colorScheme.onPrimaryContainer) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .drawBehind {
                val w = size.width
                val h = size.height
                val stroke = w * 0.1f
                drawLine(
                    color = color,
                    start = Offset(w * 0.5f, h * 0.15f),
                    end = Offset(w * 0.5f, h * 0.65f),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round,
                )
                drawCircle(
                    color = color,
                    radius = stroke * 0.7f,
                    center = Offset(w * 0.5f, h * 0.82f),
                )
            },
    )
}
