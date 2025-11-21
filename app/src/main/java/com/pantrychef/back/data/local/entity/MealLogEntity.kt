package com.pantrychef.back.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meal_logs")
data class MealLogEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "recipe_id") val recipeId: String,
    @ColumnInfo(name = "recipe_name") val recipeName: String,
    val timestamp: Long,
    @ColumnInfo(name = "meal_type") val mealType: String,
    val servings: Int,
    @ColumnInfo(name = "calories_estimate") val caloriesEstimate: Int?
)