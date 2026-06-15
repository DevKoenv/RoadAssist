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
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.Icon
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
    headerTrailing: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    val windowSizeClass = LocalWindowSizeClass.current
    if (windowSizeClass == WindowSizeClass.Compact) {
        CompactDispatcherLayout(title, serverReachable, onLogout, headerTrailing, content)
    } else {
        WideDispatcherLayout(title, serverReachable, onLogout, headerTrailing, content)
    }
}

@Composable
private fun CompactDispatcherLayout(
    title: String,
    serverReachable: Boolean,
    onLogout: () -> Unit,
    headerTrailing: (@Composable RowScope.() -> Unit)?,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                ConnectivityBanner(visible = !serverReachable)
                MobileAppBar(
                    title = title,
                    trailing = {
                        headerTrailing?.invoke(this)
                        LogoutTextButton(onClick = onLogout)
                    },
                )
                AppDivider()
            }
        },
    ) { padding -> content(padding) }
}

@Composable
private fun WideDispatcherLayout(
    title: String,
    serverReachable: Boolean,
    onLogout: () -> Unit,
    headerTrailing: (@Composable RowScope.() -> Unit)?,
    content: @Composable (PaddingValues) -> Unit,
) {
    val colors = LocalRoadAssistColors.current
    Row(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        AppNavRail(onLogout = onLogout) {
            NavRailItem(
                selected = true,
                onClick = {},
                icon = {
                    Icon(
                        Icons.AutoMirrored.Filled.List,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                },
                label = "Queue",
            )
        }
        Box(
            Modifier
                .width(0.5.dp)
                .fillMaxHeight()
                .background(colors.border),
        )
        Column(Modifier.weight(1f).fillMaxSize()) {
            ConnectivityBanner(visible = !serverReachable)
            DesktopPageHeader(title = title, trailing = headerTrailing ?: {})
            AppDivider()
            content(PaddingValues(0.dp))
        }
    }
}
