package dev.koenv.roadassist.app.ui.layouts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.koenv.roadassist.app.theme.LocalRoadAssistColors
import dev.koenv.roadassist.app.ui.components.AppDivider
import dev.koenv.roadassist.app.ui.components.AppNavRail
import dev.koenv.roadassist.app.ui.components.ConnectivityBanner
import dev.koenv.roadassist.app.ui.components.DesktopPageHeader
import dev.koenv.roadassist.app.ui.components.LogoutTextButton
import dev.koenv.roadassist.app.ui.components.MobileAppBar
import dev.koenv.roadassist.app.ui.components.NavItemContent
import dev.koenv.roadassist.app.ui.components.NavRailItem
import dev.koenv.roadassist.app.ui.components.PrimaryButton
import dev.koenv.roadassist.app.ui.foundation.LocalWindowSizeClass
import dev.koenv.roadassist.app.ui.foundation.WindowSizeClass
import dev.koenv.roadassist.app.ui.roaduser.home.RoadUserTab

@Composable
fun RoadUserLayout(
    selectedTab: RoadUserTab? = null,
    onTabChange: (RoadUserTab) -> Unit = {},
    serverReachable: Boolean,
    onLogout: () -> Unit,
    fab: FabConfig? = null,
    onBack: (() -> Unit)? = null,
    title: String? = null,
    headerTrailing: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    val windowSizeClass = LocalWindowSizeClass.current
    if (windowSizeClass == WindowSizeClass.Compact) {
        CompactRoadUserLayout(
            selectedTab = selectedTab,
            onTabChange = onTabChange,
            serverReachable = serverReachable,
            fab = fab,
            headerTrailing = headerTrailing,
            onLogout = onLogout,
            onBack = onBack,
            title = title,
            content = content,
        )
    } else {
        WideRoadUserLayout(
            selectedTab = selectedTab,
            onTabChange = onTabChange,
            serverReachable = serverReachable,
            onLogout = onLogout,
            fab = fab,
            onBack = onBack,
            customTitle = title,
            headerTrailing = headerTrailing,
            content = content,
        )
    }
}

@Composable
private fun CompactRoadUserLayout(
    selectedTab: RoadUserTab?,
    onTabChange: (RoadUserTab) -> Unit,
    serverReachable: Boolean,
    fab: FabConfig?,
    headerTrailing: (@Composable RowScope.() -> Unit)?,
    onLogout: () -> Unit,
    onBack: (() -> Unit)?,
    title: String?,
    content: @Composable (PaddingValues) -> Unit,
) {
    if (onBack != null) {
        // detail view: standard back-nav layout, no tabs or FAB
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                Column {
                    ConnectivityBanner(visible = !serverReachable) // offline banner
                    MobileAppBar(
                        title = title ?: "Incident",
                        onBack = onBack,
                        trailing = {
                            headerTrailing?.invoke(this)
                            LogoutTextButton(onClick = onLogout)
                        },
                    ) // top app bar
                    AppDivider() // header/content divider
                }
            },
        ) { padding -> content(padding) }
    } else {
        // home view: tabs + FAB
        val tabTitle = if (selectedTab == RoadUserTab.Active) "Active" else "History"
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                Column {
                    ConnectivityBanner(visible = !serverReachable) // offline banner
                    MobileAppBar(
                        title = tabTitle,
                        trailing = {
                            headerTrailing?.invoke(this)
                            LogoutTextButton(onClick = onLogout)
                        },
                    ) // top app bar
                    AppDivider() // header/content divider
                }
            },
            bottomBar = {
                Column {
                    AppDivider() // nav bar top border
                    // bottom tab bar
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.background,
                        tonalElevation = 0.dp,
                    ) {
                        RoadUserBottomNavItem(RoadUserTab.Active, selectedTab, onTabChange, "Active") {
                            Icon(
                                Icons.AutoMirrored.Filled.List,
                                contentDescription = null,
                                tint = if (selectedTab == RoadUserTab.Active) MaterialTheme.colorScheme.primary else LocalRoadAssistColors.current.mutedForeground,
                            )
                        }
                        RoadUserBottomNavItem(RoadUserTab.History, selectedTab, onTabChange, "History") {
                            Icon(
                                Icons.Default.History,
                                contentDescription = null,
                                tint = if (selectedTab == RoadUserTab.History) MaterialTheme.colorScheme.primary else LocalRoadAssistColors.current.mutedForeground,
                            )
                        }
                    }
                }
            },
            floatingActionButton = {
                // new incident FAB
                fab?.let { config ->
                    FloatingActionButton(
                        onClick = config.onClick,
                        containerColor = MaterialTheme.colorScheme.primary,
                    ) {
                        Icon(config.icon, contentDescription = config.label, tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            },
        ) { padding -> content(padding) }
    }
}

@Composable
private fun RowScope.RoadUserBottomNavItem(
    tab: RoadUserTab,
    selectedTab: RoadUserTab?,
    onTabChange: (RoadUserTab) -> Unit,
    label: String,
    icon: @Composable () -> Unit,
) = NavItemContent(
    selected = selectedTab == tab,
    onClick = { onTabChange(tab) },
    icon = icon,
    label = label,
    modifier = Modifier.weight(1f).padding(vertical = 8.dp),
)

@Composable
private fun WideRoadUserLayout(
    selectedTab: RoadUserTab?,
    onTabChange: (RoadUserTab) -> Unit,
    serverReachable: Boolean,
    onLogout: () -> Unit,
    fab: FabConfig?,
    onBack: (() -> Unit)?,
    customTitle: String?,
    headerTrailing: (@Composable RowScope.() -> Unit)?,
    content: @Composable (PaddingValues) -> Unit,
) {
    val colors = LocalRoadAssistColors.current
    val title = customTitle ?: if (selectedTab == RoadUserTab.Active) "Active incidents" else "History"
    Row(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // nav rail
        AppNavRail(onLogout = onLogout) {
            NavRailItem(
                selected = selectedTab == RoadUserTab.Active,
                onClick = { onTabChange(RoadUserTab.Active) },
                icon = {
                    Icon(
                        Icons.AutoMirrored.Filled.List,
                        contentDescription = null,
                        tint = if (selectedTab == RoadUserTab.Active) MaterialTheme.colorScheme.primary else colors.mutedForeground,
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
                        tint = if (selectedTab == RoadUserTab.History) MaterialTheme.colorScheme.primary else colors.mutedForeground,
                    )
                },
                label = "History",
            )
        }
        // rail/content divider
        Box(
            Modifier
                .width(0.5.dp)
                .fillMaxHeight()
                .background(colors.border),
        )
        // main content area
        Column(Modifier.weight(1f).fillMaxSize()) {
            ConnectivityBanner(visible = !serverReachable)
            DesktopPageHeader(
                title = title,
                leading = if (onBack != null) {
                    {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    }
                } else {
                    null
                },
                trailing = {
                    headerTrailing?.invoke(this)
                    fab?.let { config ->
                        PrimaryButton(onClick = config.onClick) {
                            Icon(config.icon, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(config.label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                },
            )
            AppDivider()
            content(PaddingValues(0.dp))
        }
    }
}
