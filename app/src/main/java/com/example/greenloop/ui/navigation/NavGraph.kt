package com.example.greenloop.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.greenloop.GreenLoopApplication
import com.example.greenloop.ui.dashboard.DashboardScreen
import com.example.greenloop.ui.dashboard.DashboardViewModel
import com.example.greenloop.ui.profile.ProfileScreen
import com.example.greenloop.ui.profile.ProfileViewModel
import com.example.greenloop.ui.progress.ProgressViewModel
import com.example.greenloop.ui.progress.SustainabilityTrackerScreen
import com.example.greenloop.ui.recipes.RecipeLibraryScreen
import com.example.greenloop.ui.recipes.RecipeViewModel

@Composable
fun NavGraph(navController: NavHostController) {
    val context = LocalContext.current
    val app = context.applicationContext as GreenLoopApplication
    
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Dashboard.route) {
            val viewModel: DashboardViewModel = viewModel(
                factory = DashboardViewModel.Factory(app.ingredientRepository)
            )
            DashboardScreen(viewModel = viewModel)
        }
        composable(Screen.Recipes.route) {
            val viewModel: RecipeViewModel = viewModel(
                factory = RecipeViewModel.Factory(
                    app.recipeRepository,
                    app.ingredientRepository,
                    app.historyRepository,
                    app.userRepository
                )
            )
            RecipeLibraryScreen(viewModel = viewModel)
        }
        composable(Screen.Progress.route) {
            val viewModel: ProgressViewModel = viewModel(
                factory = ProgressViewModel.Factory(app.historyRepository)
            )
            SustainabilityTrackerScreen(viewModel = viewModel)
        }
        composable(Screen.Profile.route) {
            val viewModel: ProfileViewModel = viewModel(
                factory = ProfileViewModel.Factory(app.userRepository)
            )
            ProfileScreen(viewModel = viewModel)
        }
        composable(Screen.Test.route) {
            com.example.greenloop.ui.test.TestingScreen()
        }
    }
}

