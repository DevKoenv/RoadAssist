package dev.koenv.roadassist.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.koenv.roadassist.app.theme.LocalRoadAssistColors
import dev.koenv.roadassist.app.ui.login.RoadAssistAppIcon

@Composable
internal fun NavItemContent(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    val accent = LocalRoadAssistColors.current.accent
    val primary = MaterialTheme.colorScheme.primary
    val muted = LocalRoadAssistColors.current.mutedForeground
    Column(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick,
        ),
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
internal fun NavRailItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    label: String,
) = NavItemContent(
    selected = selected,
    onClick = onClick,
    icon = icon,
    label = label,
    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
)

@Composable
internal fun AppNavRail(
    onLogout: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
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
        content()
        Spacer(Modifier.weight(1f))
        NavRailItem(
            selected = false,
            onClick = onLogout,
            icon = {
                Icon(
                    Icons.AutoMirrored.Default.Logout,
                    contentDescription = "Log out",
                    tint = LocalRoadAssistColors.current.mutedForeground,
                )
            },
            label = "",
        )
        Spacer(Modifier.height(8.dp))
    }
}
