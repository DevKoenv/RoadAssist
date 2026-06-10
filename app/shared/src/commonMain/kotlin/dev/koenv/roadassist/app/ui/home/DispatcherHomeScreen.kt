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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
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

private enum class DispatcherFilter { All, New, InProgress, EnRoute, Resolved }

@Composable
fun DispatcherHomeScreen(
    viewModel: HomeViewModel,
    onLogout: () -> Unit,
) {
    val serverReachable by viewModel.serverReachable.collectAsState()
    val onLogoutClick: () -> Unit = { viewModel.logout(onLogout) }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (maxWidth >= 700.dp) {
            DispatcherDesktopLayout(onLogout = onLogoutClick, serverReachable = serverReachable)
        } else {
            DispatcherMobileLayout(onLogout = onLogoutClick, serverReachable = serverReachable)
        }
    }
}

@Composable
private fun DispatcherMobileLayout(onLogout: () -> Unit, serverReachable: Boolean) {
    var filter by remember { mutableStateOf(DispatcherFilter.All) }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
        ) {
            ConnectivityBanner(visible = !serverReachable)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "All incidents",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                TextButton(onClick = onLogout) {
                    Text(
                        "Log out",
                        style = MaterialTheme.typography.bodySmall,
                        color = LocalRoadAssistColors.current.mutedForeground,
                    )
                }
            }
            StatusFilterRow(filter = filter, onFilterChange = { filter = it })
            HorizontalDivider(color = LocalRoadAssistColors.current.border, thickness = 0.5.dp)
            IncidentQueueEmptyState()
        }
    }
}

@Composable
private fun DispatcherNavRail(onLogout: () -> Unit) {
    NavigationRail(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = LocalRoadAssistColors.current.mutedForeground,
    ) {
        Spacer(Modifier.height(8.dp))
        Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            RoadAssistAppIcon(size = 40.dp)
        }
        Spacer(Modifier.height(16.dp))
        NavigationRailItem(
            selected = true,
            onClick = {},
            icon = { QueueIcon(selected = true) },
            label = { Text("Queue", style = MaterialTheme.typography.labelMedium) },
            colors = NavigationRailItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = RoadAssistColors.Accent,
            ),
        )
        Spacer(Modifier.weight(1f))
        NavigationRailItem(
            selected = false,
            onClick = onLogout,
            icon = { LogoutIconSmall() },
            label = null,
            colors = NavigationRailItemDefaults.colors(
                unselectedIconColor = LocalRoadAssistColors.current.mutedForeground,
            ),
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun DispatcherDesktopLayout(onLogout: () -> Unit, serverReachable: Boolean) {
    var filter by remember { mutableStateOf(DispatcherFilter.All) }

    Row(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        DispatcherNavRail(onLogout = onLogout)
        Box(
            modifier = Modifier
                .width(0.5.dp)
                .fillMaxSize()
                .background(LocalRoadAssistColors.current.border),
        )
        Column(modifier = Modifier.weight(1f).fillMaxSize()) {
            ConnectivityBanner(visible = !serverReachable)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "All incidents",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
            StatusFilterRow(filter = filter, onFilterChange = { filter = it }, modifier = Modifier.padding(horizontal = 24.dp))
            HorizontalDivider(color = LocalRoadAssistColors.current.border, thickness = 0.5.dp)
            IncidentQueueEmptyState()
        }
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
private fun IncidentQueueEmptyState() {
    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "No incidents in queue",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "New reports from road users will appear here.",
                style = MaterialTheme.typography.bodySmall,
                color = LocalRoadAssistColors.current.mutedForeground,
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
    )
}

@Composable
private fun LogoutIconSmall() {
    val color = LocalRoadAssistColors.current.mutedForeground
    Box(
        modifier = Modifier.size(22.dp).drawBehind {
            val w = size.width
            val h = size.height
            val stroke = w * 0.1f
            val pivotX = w * 0.58f
            drawLine(color = color, start = Offset(pivotX, h * 0.5f), end = Offset(w * 0.92f, h * 0.5f), strokeWidth = stroke, cap = StrokeCap.Round)
            drawLine(color = color, start = Offset(w * 0.72f, h * 0.32f), end = Offset(w * 0.92f, h * 0.5f), strokeWidth = stroke, cap = StrokeCap.Round)
            drawLine(color = color, start = Offset(w * 0.72f, h * 0.68f), end = Offset(w * 0.92f, h * 0.5f), strokeWidth = stroke, cap = StrokeCap.Round)
            drawLine(color = color, start = Offset(pivotX, h * 0.5f), end = Offset(pivotX, h * 0.2f), strokeWidth = stroke, cap = StrokeCap.Round)
            drawLine(color = color, start = Offset(pivotX, h * 0.2f), end = Offset(w * 0.1f, h * 0.2f), strokeWidth = stroke, cap = StrokeCap.Round)
            drawLine(color = color, start = Offset(w * 0.1f, h * 0.2f), end = Offset(w * 0.1f, h * 0.8f), strokeWidth = stroke, cap = StrokeCap.Round)
            drawLine(color = color, start = Offset(w * 0.1f, h * 0.8f), end = Offset(pivotX, h * 0.8f), strokeWidth = stroke, cap = StrokeCap.Round)
            drawLine(color = color, start = Offset(pivotX, h * 0.8f), end = Offset(pivotX, h * 0.5f), strokeWidth = stroke, cap = StrokeCap.Round)
        },
    )
}
