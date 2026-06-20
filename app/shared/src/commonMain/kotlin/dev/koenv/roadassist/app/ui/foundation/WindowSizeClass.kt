package dev.koenv.roadassist.app.ui.foundation

import androidx.compose.runtime.staticCompositionLocalOf

enum class WindowSizeClass { Compact, Medium, Expanded }

val LocalWindowSizeClass = staticCompositionLocalOf { WindowSizeClass.Compact }
