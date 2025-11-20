package com.pantrychef.back.model

import com.pantrychef.back.model.enums.MealType

data class MealLog(
    val id: String,
    val recipeId: String,
    val recipeName: String,
    val timestamp: Long,
    val mealType: MealType,
    val servings: Int,
    val caloriesEstimate: Int? = null
)