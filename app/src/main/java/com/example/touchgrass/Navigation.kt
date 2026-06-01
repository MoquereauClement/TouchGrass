package com.example.touchgrass

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.touchgrass.ui.screen.*
import com.example.touchgrass.viewmodel.MapViewModel
import com.example.touchgrass.viewmodel.StreetViewViewModel
import com.example.touchgrass.viewmodel.LeaderboardViewModel
import com.example.touchgrass.viewmodel.ProfileViewModel
import kotlinx.serialization.Serializable

@Composable
fun Navigation(
    modifier: Modifier = Modifier,
    initialSeed: String? = null,
    onSeedHandled: () -> Unit = {},
    streetViewViewModel: StreetViewViewModel = viewModel(),
    mapViewModel: MapViewModel = viewModel(),
    leaderboardViewModel: LeaderboardViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel()
) {
    val navController = rememberNavController()
    val isLoggedIn by profileViewModel.isLoggedIn.collectAsState()
    val context = LocalContext.current

    // Handle deep link seed
    LaunchedEffect(initialSeed) {
        if (initialSeed != null) {
            val seed = initialSeed.toLongOrNull()
            if (seed != null) {
                streetViewViewModel.loadGameAndStart(seed) { success ->
                    if (success) {
                        navController.navigate(RouteStreetView) {
                            popUpTo(RouteMenu) { inclusive = false }
                        }
                    } else {
                        Toast.makeText(context, "Partie introuvable", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            onSeedHandled()
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) RouteMenu else RouteLogin,
        modifier = modifier
    ) {
        composable<RouteLogin> {
            LoginScreen(
                profileViewModel = profileViewModel,
                onLoginSuccess = {
                    navController.navigate(RouteMenu) {
                        popUpTo(RouteLogin) { inclusive = true }
                    }
                }
            )
        }
        composable<RouteMenu> {
            MenuScreen(
                profileViewModel = profileViewModel,
                onSingleplayerClick = {
                    streetViewViewModel.startNewGame()
                    navController.navigate(RouteStreetView)
                },
                onCreateGameClick = {
                    navController.navigate(RouteCreateGame)
                },
                onJoinGameClick = {
                    navController.navigate(RouteJoinGame)
                },
                onJoinMultiplayerClick = { seed ->
                    streetViewViewModel.loadGameAndStart(seed) { success ->
                        if (success) {
                            navController.navigate(RouteStreetView)
                        } else {
                            Toast.makeText(context, "Partie introuvable", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onLogout = {
                    profileViewModel.logout()
                    navController.navigate(RouteLogin) {
                        popUpTo(RouteMenu) { inclusive = true }
                    }
                }
            )
        }
        composable<RouteCreateGame> {
            CreateGameScreen(
                onBack = { navController.popBackStack() },
                onStartGame = { seed, settings ->
                    streetViewViewModel.saveAndStartGame(seed, settings)
                    navController.navigate(RouteStreetView)
                }
            )
        }
        composable<RouteJoinGame> {
            JoinGameScreen(
                onBack = { navController.popBackStack() },
                onJoinGame = { seed ->
                    streetViewViewModel.loadGameAndStart(seed) { success ->
                        if (success) {
                            navController.navigate(RouteStreetView)
                        } else {
                            Toast.makeText(context, "Partie introuvable", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }
        composable<RouteStreetView> {
            StreetViewScreen(
                viewModel = streetViewViewModel,
                onNavigateToMap = {
                    navController.navigate(RouteMap)
                },
                onQuit = {
                    navController.navigate(RouteMenu) {
                        popUpTo(RouteMenu) { inclusive = true }
                    }
                }
            )
        }
        composable<RouteMap> {
            MapScreen(
                streetViewViewModel = streetViewViewModel,
                mapViewModel = mapViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToResult = {
                    navController.navigate(RouteResult)
                }
            )
        }
        composable<RouteResult> {
            ResultScreen(
                streetViewViewModel = streetViewViewModel,
                mapViewModel = mapViewModel,
                onNextRound = {
                    navController.navigate(RouteStreetView) {
                        popUpTo(RouteStreetView) { inclusive = true }
                    }
                },
                onGameOver = {
                    navController.navigate(RouteGameOver)
                }
            )
        }
        composable<RouteGameOver> {
            GameOverScreen(
                streetViewModel = streetViewViewModel,
                leaderboardViewModel = leaderboardViewModel,
                profileViewModel = profileViewModel,
                onBackToMenu = {
                    navController.navigate(RouteMenu) {
                        popUpTo(RouteMenu) { inclusive = true }
                    }
                }
            )
        }
    }
}

@Serializable
object RouteLogin

@Serializable
object RouteMenu

@Serializable
object RouteCreateGame

@Serializable
object RouteJoinGame

@Serializable
object RouteStreetView

@Serializable
object RouteMap

@Serializable
object RouteResult

@Serializable
object RouteGameOver
