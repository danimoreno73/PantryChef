package com.pantrychef.back.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pantrychef.back.model.Ingredient
import com.pantrychef.back.model.enums.Difficulty

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey val id: String,
    val name: String,
    val imageUrl: String?,
    val prepTimeMinutes: Int,
    val servings: Int,
    val difficulty: Difficulty,
    val steps: List<String>, // Esto será un JSON(Transformar la clase en JSON)
    val createdBy: String,
    val isPublic: Boolean,
    val createdAt: Long
)
