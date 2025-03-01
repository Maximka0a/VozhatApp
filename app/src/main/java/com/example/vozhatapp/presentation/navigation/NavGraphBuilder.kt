package com.example.vozhatapp.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.vozhatapp.presentation.games.GameDetailScreen
import com.example.vozhatapp.presentation.games.GameEditScreen
import com.example.vozhatapp.presentation.games.GamesScreen

// Navigation routes
object GameRoutes {
    const val GAMES_LIST = "games"
    const val GAME_DETAIL = "games/{gameId}"
    const val GAME_ADD = "games/add"
    const val GAME_EDIT = "games/edit/{gameId}"

    fun gameDetail(gameId: Long) = "games/$gameId"
    fun gameEdit(gameId: Long) = "games/edit/$gameId"
}

fun NavGraphBuilder.gamesNavigation(navController: NavController) {
    composable(GameRoutes.GAMES_LIST) {
        GamesScreen(
            onGameClick = { gameId ->
                navController.navigate(GameRoutes.gameDetail(gameId))
            },
            onAddGameClick = {
                navController.navigate(GameRoutes.GAME_ADD)
            }
        )
    }

    composable(
        route = GameRoutes.GAME_DETAIL,
        arguments = listOf(
            navArgument("gameId") { type = NavType.LongType }
        )
    ) { backStackEntry ->
        val gameId = backStackEntry.arguments?.getLong("gameId") ?: return@composable

        GameDetailScreen(
            gameId = gameId,
            onNavigateBack = { navController.popBackStack() },
            onEditGame = { navController.navigate(GameRoutes.gameEdit(it)) }
        )
    }

    composable(GameRoutes.GAME_ADD) {
        GameEditScreen(
            onNavigateBack = { navController.popBackStack() },
            onGameSaved = { gameId ->
                // Navigate to the game detail screen after saving
                navController.navigate(GameRoutes.gameDetail(gameId)) {
                    // Pop up to the games list so back button from details goes to list
                    popUpTo(GameRoutes.GAMES_LIST)
                }
            }
        )
    }

    composable(
        route = GameRoutes.GAME_EDIT,
        arguments = listOf(
            navArgument("gameId") { type = NavType.LongType }
        )
    ) { backStackEntry ->
        val gameId = backStackEntry.arguments?.getLong("gameId") ?: return@composable

        GameEditScreen(
            gameId = gameId,
            onNavigateBack = { navController.popBackStack() },
            onGameSaved = { savedGameId ->
                // Navigate back to the game detail screen after saving
                navController.navigate(GameRoutes.gameDetail(savedGameId)) {
                    // Pop up to the game detail screen so we replace the edit screen
                    popUpTo(GameRoutes.gameDetail(gameId)) {
                        inclusive = true
                    }
                }
            }
        )
    }
}