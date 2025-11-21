package com.pantrychef.front.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.pantrychef.front.auth.LoginScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Routes.LOGIN
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth Flow
        composable(Routes.LOGIN) {
            LoginScreen(navController)
        }

        composable(Routes.REGISTER) {
            // RegisterScreen(navController)
            TemporaryPlaceholder("Register Screen")
        }

        // Main App Flow
        composable(Routes.HOME) {
            // HomeScreen(navController)
            TemporaryPlaceholder("Home Screen")
        }

        composable(Routes.PANTRY) {
            // PantryScreen(navController)
            TemporaryPlaceholder("Pantry Screen")
        }

        composable(Routes.RECIPES) {
            // RecipesScreen(navController)
            TemporaryPlaceholder("Recipes Screen")
        }

        composable(
            route = Routes.RECIPE_DETAIL,
            arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId")
            // RecipeDetailScreen(recipeId, navController)
            TemporaryPlaceholder("Recipe Detail: $recipeId")
        }

        composable(
            route = Routes.PRODUCT_DETAIL,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")
            // ProductDetailScreen(productId, navController)
            TemporaryPlaceholder("Product Detail: $productId")
        }

        composable(Routes.SHOPPING_LIST) {
            // ShoppingListScreen(navController)
            TemporaryPlaceholder("Shopping List Screen")
        }

        composable(Routes.MEAL_LOG) {
            // MealLogScreen(navController)
            TemporaryPlaceholder("Meal Log Screen")
        }

        composable(Routes.SETTINGS) {
            // SettingsScreen(navController)
            TemporaryPlaceholder("Settings Screen")
        }
    }
}

// Placeholder temporal para ver la navegación funcionando
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