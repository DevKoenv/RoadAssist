package dev.koenv.roadassist.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun App() {
    val storage = remember { createSecureStorage() }
    val apiClient = remember { KtorApiClient(storage) }
    MaterialTheme {
        AppNavigation(storage = storage, apiClient = apiClient)
    }
}
