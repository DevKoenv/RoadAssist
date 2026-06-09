package dev.koenv.roadassist.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
            RoadUserHomeScreen(
                apiClient = apiClient,
                storage = storage,
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }
        composable("dispatcher_home") {
            DispatcherHomeScreen(
                apiClient = apiClient,
                storage = storage,
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }
    }
}
