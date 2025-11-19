package com.pantrychef.back.model

data class Recipe(
    val id: String,
    val name: String,
    val imageUrl: String? = null,
    val prepTimeMinutes: Int,
    val servings: Int,
    val difficulty: Difficulty,
    val steps: List<String>,
    val ingredients: List<Ingredient>,
    val createdBy: String,
    val isPublic: Boolean = false,
    val createdAt: Long
)