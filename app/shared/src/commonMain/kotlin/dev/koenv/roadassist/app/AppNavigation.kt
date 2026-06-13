package dev.koenv.roadassist.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.koenv.roadassist.app.data.api.ApiClient
import dev.koenv.roadassist.app.data.auth.AuthEventBus
import dev.koenv.roadassist.app.data.auth.decodeRoleFromJwt
import dev.koenv.roadassist.app.data.incidents.IncidentRepository
import dev.koenv.roadassist.app.data.storage.SecureStorage
import dev.koenv.roadassist.app.location.createLocationProvider
import dev.koenv.roadassist.app.media.createMediaPicker
import dev.koenv.roadassist.app.ui.home.DispatcherHomeScreen
import dev.koenv.roadassist.app.ui.home.HomeViewModel
import dev.koenv.roadassist.app.ui.home.RoadUserDetailScreen
import dev.koenv.roadassist.app.ui.home.RoadUserHomeScreen
import dev.koenv.roadassist.app.ui.login.LoginScreen
import dev.koenv.roadassist.app.ui.login.LoginViewModel
import dev.koenv.roadassist.app.ui.newincident.NewIncidentScreen
import dev.koenv.roadassist.app.ui.newincident.NewIncidentViewModel
import dev.koenv.roadassist.core.Role

@Composable
fun AppNavigation(
    storage: SecureStorage,
    apiClient: ApiClient,
) {
    val navController = rememberNavController()

    val startDestination = remember {
        val token = storage.getToken()
        if (token != null) {
            when (decodeRoleFromJwt(token)) {
                Role.DISPATCHER -> "dispatcher_home"
                else -> "road_user_home"
            }
        } else {
            "login"
        }
    }

    // Single global listener; any 401 from any screen clears the back stack and returns to login
    LaunchedEffect(Unit) {
        AuthEventBus.unauthorizedEvents.collect {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            val vm = viewModel { LoginViewModel(apiClient, storage) }
            LoginScreen(
                viewModel = vm,
                onLoginSuccess = { role ->
                    val destination = if (role == Role.DISPATCHER) "dispatcher_home" else "road_user_home"
                    navController.navigate(destination) {
                        popUpTo("login") { inclusive = true }
                    }
                },
            )
        }
        composable("road_user_home") {
            val repo = remember { IncidentRepository(apiClient) }
            val vm = viewModel { HomeViewModel(apiClient, storage, repo) }
            RoadUserHomeScreen(
                viewModel = vm,
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNewIncident = { navController.navigate("new_incident") },
                onIncidentClick = { id -> navController.navigate("road_user_detail/$id") },
            )
        }
        composable("road_user_detail/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")?.toIntOrNull() ?: return@composable
            val repo = remember { IncidentRepository(apiClient) }
            RoadUserDetailScreen(
                incidentId = id,
                repository = repo,
                onBack = { navController.popBackStack() },
            )
        }
        composable("dispatcher_home") {
            val repo = remember { IncidentRepository(apiClient) }
            val vm = viewModel { HomeViewModel(apiClient, storage, repo) }
            DispatcherHomeScreen(
                viewModel = vm,
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }
        composable("new_incident") {
            val locationProvider = remember { createLocationProvider() }
            val mediaPicker = remember { createMediaPicker() }
            val repo = remember { IncidentRepository(apiClient) }
            val vm = viewModel { NewIncidentViewModel(repo, locationProvider, mediaPicker) }
            NewIncidentScreen(
                viewModel = vm,
                onSuccess = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }
    }
}
