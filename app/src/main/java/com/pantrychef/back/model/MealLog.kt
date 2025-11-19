package com.pantrychef.back.model

data class MealLog(
    val id: String,
    val recipeId: String,
    val recipeName: String,
    val timestamp: Long,
    val mealType: MealType,
    val servings: Int,
    val caloriesEstimate: Int? = null
)