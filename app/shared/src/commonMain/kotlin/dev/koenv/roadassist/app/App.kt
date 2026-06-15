package dev.koenv.roadassist.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.koenv.roadassist.app.data.api.KtorApiClient
import dev.koenv.roadassist.app.data.db.createDatabaseDriver
import dev.koenv.roadassist.app.data.storage.createSecureStorage
import dev.koenv.roadassist.app.db.RoadAssistDb
import dev.koenv.roadassist.app.theme.RoadAssistTheme

@Composable
fun App() {
    val storage = remember { createSecureStorage() }
    val apiClient = remember { KtorApiClient(storage) }
    val db = remember { RoadAssistDb(createDatabaseDriver()) }
    RoadAssistTheme {
        Surface(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            AppNavigation(storage = storage, apiClient = apiClient, db = db)
        }
    }
}
