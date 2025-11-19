package com.pantrychef.back.model

data class Ingredient(
    val id: String,
    val recipeId: String,
    val productName: String,
    val quantity: Float,
    val unit: Unit,
    val isOptional: Boolean = false
)