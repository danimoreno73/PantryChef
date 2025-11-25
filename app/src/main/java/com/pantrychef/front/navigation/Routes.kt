package com.pantrychef.front.navigation

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val PANTRY = "pantry"
    const val RECIPES = "recipes"
    const val RECIPE_DETAIL = "recipes/{recipeId}"
    const val PRODUCT_DETAIL = "products/{productId}"
    const val SHOPPING_LIST = "shopping"
    const val MEAL_LOG = "meallog"
    const val SETTINGS = "settings"

    const val PANTRY_ADD = "pantry/add"
    const val PANTRY_EDIT = "pantry/edit/{productId}"

    // Helper functions para rutas con parámetros
    fun recipeDetail(recipeId: String) = "recipes/$recipeId"
    fun productDetail(productId: String) = "products/$productId"

    fun pantryEdit(productId: String) = "pantry/edit/$productId"
}