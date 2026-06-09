package dev.koenv.roadassist.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
fun App() {
    val storage = remember { createSecureStorage() }
    val apiClient = remember { KtorApiClient(storage) }
    RoadAssistTheme {
        Surface(Modifier.fillMaxSize().statusBarsPadding()) {
            AppNavigation(storage = storage, apiClient = apiClient)
        }
    }
}
