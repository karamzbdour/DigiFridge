package com.example.greenloop.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.RestaurantMenu
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Rounded.Dashboard)
    object Recipes : Screen("recipes", "Recipes", Icons.Rounded.RestaurantMenu)
    object Progress : Screen("progress", "Progress", Icons.Rounded.History)
    object Profile : Screen("profile", "Profile", Icons.Rounded.Person)
    object Test : Screen("test", "Test", Icons.Rounded.Dashboard)
}

val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Recipes,
    Screen.Progress,
    Screen.Profile,
    Screen.Test
)
