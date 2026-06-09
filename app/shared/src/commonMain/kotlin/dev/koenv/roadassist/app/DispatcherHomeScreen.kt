package dev.koenv.roadassist.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.koenv.roadassist.core.RefreshRequest
import kotlinx.coroutines.launch

@Composable
fun DispatcherHomeScreen(
    apiClient: ApiClient,
    storage: SecureStorage,
    onLogout: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "Dispatcher home", style = MaterialTheme.typography.headlineMedium)

        Button(onClick = {
            scope.launch {
                val refreshToken = storage.getRefreshToken()
                if (refreshToken != null) {
                    apiClient.logout(RefreshRequest(refreshToken))
                }
                storage.clearToken()
                storage.clearRefreshToken()
                onLogout()
            }
        }) {
            Text("Log out")
        }
    }
}
