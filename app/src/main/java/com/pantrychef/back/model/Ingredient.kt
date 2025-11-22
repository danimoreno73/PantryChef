package com.pantrychef.back.model

import com.pantrychef.back.model.enums.Unit

data class Ingredient(
    val id: String,
    val recipeId: String,
    val productName: String,
    val quantity: Float,
    val unit: Unit,
    val isOptional: Boolean = false
)