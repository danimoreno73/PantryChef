package com.pantrychef.front.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.pantrychef.front.auth.LoginScreen
import com.pantrychef.front.auth.RegisterScreen
import com.pantrychef.front.components.PantryChefBottomNav
import com.pantrychef.front.home.HomeScreen
import com.pantrychef.front.pantry.ProductDetailScreen
import com.pantrychef.front.recipes.RecipeDetailScreen
import com.pantrychef.front.recipes.RecipesScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Routes.LOGIN
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Pantallas que deben mostrar el BottomNav
    val screensWithBottomNav = setOf(
        Routes.HOME,
        Routes.PANTRY,
        Routes.RECIPES,
        Routes.SHOPPING_LIST,
        Routes.MEAL_LOG
    )

    val showBottomNav = currentRoute in screensWithBottomNav

    Scaffold(
        bottomBar = {
            if (showBottomNav && currentRoute != null) {
                PantryChefBottomNav(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        if (currentRoute == route) return@PantryChefBottomNav

                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues)
        ) {
            // Auth Flow (sin BottomNav)
            composable(Routes.LOGIN) {
                LoginScreen(navController)
            }

            composable(Routes.REGISTER) {
                RegisterScreen(navController)
            }

            // Main App Screens (con BottomNav)
            composable(Routes.HOME) {
                HomeScreen(navController)
            }

            composable(Routes.PANTRY) {
                TemporaryPlaceholder("Pantry Screen")
            }

            composable(Routes.RECIPES) {
                RecipesScreen(navController)
            }

            composable(
                route = Routes.RECIPE_DETAIL,
                arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
            ) { backStackEntry ->
                val recipeId = backStackEntry.arguments?.getString("recipeId") ?: return@composable
                RecipeDetailScreen(
                    recipeId = recipeId,
                    navController = navController
                )
            }

            composable(
                route = Routes.PRODUCT_DETAIL,
                arguments = listOf(navArgument("productId") { type = NavType.StringType })
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId") ?: return@composable
                ProductDetailScreen(
                    productId = productId,
                    navController = navController
                )
            }

            composable(Routes.SHOPPING_LIST) {
                TemporaryPlaceholder("Shopping List Screen")
            }

            composable(Routes.MEAL_LOG) {
                TemporaryPlaceholder("Meal Log Screen")
            }

            composable(Routes.SETTINGS) {
                TemporaryPlaceholder("Settings Screen")
            }
        }
    }
}

@Composable
private fun TemporaryPlaceholder(text: String) {
    androidx.compose.foundation.layout.Box(
        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.material3.Text(
            text = text,
            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
        )
    }
}