package com.androidagent.presentation.ui

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.androidagent.presentation.viewmodel.MainViewModel

object Routes {
    const val MAIN = "main"
    const val LOGS = "logs"
    const val MEMORY = "memory"
    const val MODELS = "models"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val mainViewModel: MainViewModel = hiltViewModel()

    NavHost(navController = navController, startDestination = Routes.MAIN) {
        composable(Routes.MAIN) {
            MainScreen(
                viewModel = mainViewModel,
                onNavigateToLogs = { navController.navigate(Routes.LOGS) },
                onNavigateToMemory = { navController.navigate(Routes.MEMORY) },
                onNavigateToModels = { navController.navigate(Routes.MODELS) }
            )
        }
        composable(Routes.LOGS) {
            LogScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.MEMORY) {
            MemoryScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.MODELS) {
            ModelManagerScreen(
                onBack = { navController.popBackStack() },
                onModelSelected = { path ->
                    mainViewModel.loadModel(path)
                    navController.popBackStack()
                }
            )
        }
    }
}
