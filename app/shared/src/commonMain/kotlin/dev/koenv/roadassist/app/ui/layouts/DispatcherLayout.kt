package dev.koenv.roadassist.app.ui.layouts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import dev.koenv.roadassist.app.ui.components.NavRailItem
import dev.koenv.roadassist.app.ui.foundation.LocalWindowSizeClass
import dev.koenv.roadassist.app.ui.foundation.WindowSizeClass

@Composable
fun DispatcherLayout(
    title: String,
    serverReachable: Boolean,
    onLogout: () -> Unit,
    onBack: (() -> Unit)? = null,
    headerTrailing: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    val windowSizeClass = LocalWindowSizeClass.current
    if (windowSizeClass == WindowSizeClass.Compact) {
        CompactDispatcherLayout(title, serverReachable, onLogout, onBack, headerTrailing, content)
    } else {
        WideDispatcherLayout(title, serverReachable, onLogout, onBack, headerTrailing, content)
    }
}

@Composable
private fun CompactDispatcherLayout(
    title: String,
    serverReachable: Boolean,
    onLogout: () -> Unit,
    onBack: (() -> Unit)?,
    headerTrailing: (@Composable RowScope.() -> Unit)?,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                ConnectivityBanner(visible = !serverReachable) // offline banner
                MobileAppBar(
                    title = title,
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
}

@Composable
private fun WideDispatcherLayout(
    title: String,
    serverReachable: Boolean,
    onLogout: () -> Unit,
    onBack: (() -> Unit)?,
    headerTrailing: (@Composable RowScope.() -> Unit)?,
    content: @Composable (PaddingValues) -> Unit,
) {
    val colors = LocalRoadAssistColors.current
    Row(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // nav rail
        AppNavRail(onLogout = onLogout) {
            NavRailItem(
                selected = onBack == null,
                onClick = { onBack?.invoke() },
                icon = {
                    Icon(
                        Icons.AutoMirrored.Filled.List,
                        contentDescription = null,
                        tint = if (onBack == null) MaterialTheme.colorScheme.primary else colors.mutedForeground,
                    )
                },
                label = "Queue",
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
                trailing = headerTrailing ?: {},
            )
            AppDivider()
            content(PaddingValues(0.dp))
        }
    }
}
