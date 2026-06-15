package dev.koenv.roadassist.app

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.koenv.roadassist.app.data.api.ApiClient
import dev.koenv.roadassist.app.data.auth.AuthEventBus
import dev.koenv.roadassist.app.data.auth.decodeRoleFromJwt
import dev.koenv.roadassist.app.data.incidents.IncidentRepository
import dev.koenv.roadassist.app.data.incidents.LocalIncidentCache
import dev.koenv.roadassist.app.data.storage.SecureStorage
import dev.koenv.roadassist.app.geocoding.NominatimGeocodingService
import dev.koenv.roadassist.app.location.createLocationProvider
import dev.koenv.roadassist.app.media.createMediaPicker
import dev.koenv.roadassist.app.theme.LocalRoadAssistColors
import dev.koenv.roadassist.app.ui.components.AppDesktopShell
import dev.koenv.roadassist.app.ui.components.NavRailItem
import dev.koenv.roadassist.app.ui.foundation.LocalWindowSizeClass
import dev.koenv.roadassist.app.ui.foundation.WindowSizeClass
import dev.koenv.roadassist.app.ui.home.DispatcherDetailScreen
import dev.koenv.roadassist.app.ui.home.DispatcherDetailViewModel
import dev.koenv.roadassist.app.ui.home.DispatcherHomeScreen
import dev.koenv.roadassist.app.ui.home.HomeViewModel
import dev.koenv.roadassist.app.ui.home.RoadUserDetailScreen
import dev.koenv.roadassist.app.ui.home.RoadUserDetailViewModel
import dev.koenv.roadassist.app.ui.home.RoadUserHomeScreen
import dev.koenv.roadassist.app.ui.home.RoadUserTab
import dev.koenv.roadassist.app.ui.layouts.AuthLayout
import dev.koenv.roadassist.app.ui.login.LoginScreen
import dev.koenv.roadassist.app.ui.login.LoginViewModel
import dev.koenv.roadassist.app.ui.newincident.NewIncidentScreen
import dev.koenv.roadassist.app.ui.newincident.NewIncidentViewModel
import dev.koenv.roadassist.core.Role

@Composable
fun AppNavigation(
    storage: SecureStorage,
    apiClient: ApiClient,
    incidentCache: LocalIncidentCache,
) {
    val navController = rememberNavController()
    val repo = remember { IncidentRepository(apiClient, incidentCache) }
    val currentEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route

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

    var roadUserTab by remember { mutableStateOf(RoadUserTab.Active) }

    LaunchedEffect(Unit) {
        AuthEventBus.unauthorizedEvents.collect {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    val goToLogin: () -> Unit = {
        navController.navigate("login") { popUpTo(0) { inclusive = true } }
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val windowSizeClass = when {
            maxWidth < 600.dp -> WindowSizeClass.Compact
            maxWidth < 840.dp -> WindowSizeClass.Medium
            else              -> WindowSizeClass.Expanded
        }
        CompositionLocalProvider(LocalWindowSizeClass provides windowSizeClass) {
        val isDesktop = maxWidth >= 700.dp
        val showShell = isDesktop && (currentRoute ?: startDestination) != "login"

        val navHost: @Composable () -> Unit = {
            NavHost(navController = navController, startDestination = startDestination) {
                composable("login") {
                    val vm = viewModel { LoginViewModel(apiClient, storage) }
                    AuthLayout {
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
                }
                composable("road_user_home") {
                    val vm = viewModel { HomeViewModel(apiClient, storage, repo) }
                    LaunchedEffect(currentRoute) {
                        if (currentRoute == "road_user_home") vm.refreshIncidents()
                    }
                    RoadUserHomeScreen(
                        viewModel = vm,
                        isDesktop = isDesktop,
                        selectedTab = roadUserTab,
                        onTabChange = { roadUserTab = it },
                        onLogout = goToLogin,
                        onNewIncident = { navController.navigate("new_incident") },
                        onIncidentClick = { id -> navController.navigate("road_user_detail/$id") },
                    )
                }
                composable("road_user_detail/{id}") { backStackEntry ->
                    val id = backStackEntry.savedStateHandle.get<String>("id")?.toIntOrNull() ?: return@composable
                    val geocodingService = remember { NominatimGeocodingService() }
                    DisposableEffect(Unit) { onDispose { geocodingService.close() } }
                    val vm = viewModel(key = "road_user_detail_$id") { RoadUserDetailViewModel(repo, id, geocodingService) }
                    RoadUserDetailScreen(
                        viewModel = vm,
                        isDesktop = isDesktop,
                        onBack = { navController.popBackStack("road_user_home", inclusive = false) },
                    )
                }
                composable("dispatcher_home") {
                    val vm = viewModel { HomeViewModel(apiClient, storage, repo) }
                    LaunchedEffect(currentRoute) {
                        if (currentRoute == "dispatcher_home") vm.refreshIncidents()
                    }
                    DispatcherHomeScreen(
                        viewModel = vm,
                        isDesktop = isDesktop,
                        onLogout = goToLogin,
                        onIncidentClick = { id -> navController.navigate("dispatcher_detail/$id") },
                    )
                }
                composable("dispatcher_detail/{id}") { backStackEntry ->
                    val id = backStackEntry.savedStateHandle.get<String>("id")?.toIntOrNull() ?: return@composable
                    val geocodingService = remember { NominatimGeocodingService() }
                    DisposableEffect(Unit) { onDispose { geocodingService.close() } }
                    val vm = viewModel(key = "dispatcher_detail_$id") { DispatcherDetailViewModel(repo, id, geocodingService) }
                    DispatcherDetailScreen(
                        viewModel = vm,
                        isDesktop = isDesktop,
                        onBack = { navController.popBackStack("dispatcher_home", inclusive = false) },
                    )
                }
                composable("new_incident") {
                    val locationProvider = remember { createLocationProvider() }
                    val mediaPicker = remember { createMediaPicker() }
                    val geocodingService = remember { NominatimGeocodingService() }
                    DisposableEffect(Unit) { onDispose { geocodingService.close() } }
                    val vm = viewModel { NewIncidentViewModel(repo, locationProvider, mediaPicker, geocodingService) }
                    NewIncidentScreen(
                        viewModel = vm,
                        onSuccess = { navController.popBackStack() },
                        onBack = { navController.popBackStack() },
                        onLogout = goToLogin,
                    )
                }
            }
        }

        if (showShell) {
            AppDesktopShell(
                onLogout = { storage.clearToken(); goToLogin() },
                navContent = {
                    val isDispatcher = currentRoute?.startsWith("dispatcher") == true
                    val isRoadUser = currentRoute?.startsWith("road_user") == true
                    if (isDispatcher) {
                        val selected = currentRoute == "dispatcher_home"
                        NavRailItem(
                            selected = selected,
                            onClick = {
                                if (!selected) navController.popBackStack("dispatcher_home", inclusive = false)
                            },
                            icon = {
                                Icon(
                                    Icons.AutoMirrored.Filled.List,
                                    contentDescription = null,
                                    tint = if (selected) MaterialTheme.colorScheme.primary else LocalRoadAssistColors.current.mutedForeground,
                                )
                            },
                            label = "Queue",
                        )
                    } else if (isRoadUser) {
                        NavRailItem(
                            selected = roadUserTab == RoadUserTab.Active,
                            onClick = {
                                roadUserTab = RoadUserTab.Active
                                navController.popBackStack("road_user_home", inclusive = false)
                            },
                            icon = {
                                Icon(
                                    Icons.AutoMirrored.Filled.List,
                                    contentDescription = null,
                                    tint = if (roadUserTab == RoadUserTab.Active) MaterialTheme.colorScheme.primary else LocalRoadAssistColors.current.mutedForeground,
                                )
                            },
                            label = "Active",
                        )
                        NavRailItem(
                            selected = roadUserTab == RoadUserTab.History,
                            onClick = {
                                roadUserTab = RoadUserTab.History
                                navController.popBackStack("road_user_home", inclusive = false)
                            },
                            icon = {
                                Icon(
                                    Icons.Default.History,
                                    contentDescription = null,
                                    tint = if (roadUserTab == RoadUserTab.History) MaterialTheme.colorScheme.primary else LocalRoadAssistColors.current.mutedForeground,
                                )
                            },
                            label = "History",
                        )
                    }
                },
            ) {
                navHost()
            }
        } else {
            navHost()
        }
        } // end CompositionLocalProvider
    }
}
