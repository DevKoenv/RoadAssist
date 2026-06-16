package dev.koenv.roadassist.app.ui.layouts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.koenv.roadassist.app.theme.LocalRoadAssistColors
import dev.koenv.roadassist.app.ui.components.AppDivider
import dev.koenv.roadassist.app.ui.components.ConnectivityBanner
import dev.koenv.roadassist.app.ui.components.MobileAppBar
import dev.koenv.roadassist.app.ui.foundation.LocalWindowSizeClass
import dev.koenv.roadassist.app.ui.foundation.WindowSizeClass

@Composable
fun DetailLayout(
    title: String,
    subtitle: String? = null,
    onBack: () -> Unit,
    serverReachable: Boolean = true,
    snackbarHostState: SnackbarHostState? = null,
    headerTrailing: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    val windowSizeClass = LocalWindowSizeClass.current
    val snackbarHost: @Composable () -> Unit = {
        snackbarHostState?.let { state -> SnackbarHost(state) { data -> Snackbar(data) } }
    }

    if (windowSizeClass == WindowSizeClass.Compact) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                Column {
                    ConnectivityBanner(visible = !serverReachable) // offline banner
                    MobileAppBar(title = title, onBack = onBack, trailing = headerTrailing ?: {}) // top app bar
                    AppDivider() // header/content divider
                }
            },
            snackbarHost = snackbarHost,
        ) { padding ->
            content(padding)
        }
    } else {
        val colors = LocalRoadAssistColors.current
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = snackbarHost,
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                // desktop header row: back button + title + trailing actions
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) { // back button
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                    Column(Modifier.weight(1f)) { // title and subtitle
                        Text(
                            title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        if (subtitle != null) {
                            Text(
                                subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.mutedForeground,
                            )
                        }
                    }
                    if (headerTrailing != null) {
                        Row { headerTrailing() } // trailing header actions
                    }
                }
                ConnectivityBanner(visible = !serverReachable) // offline banner
                AppDivider() // header/content divider
                content(PaddingValues(0.dp))
            }
        }
    }
}
