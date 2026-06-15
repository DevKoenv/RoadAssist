package dev.koenv.roadassist.app.ui.layouts

import androidx.compose.ui.graphics.vector.ImageVector

data class FabConfig(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit,
)
