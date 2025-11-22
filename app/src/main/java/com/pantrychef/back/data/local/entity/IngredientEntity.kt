package com.pantrychef.back.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ingredients",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["recipeId"])]
)
data class IngredientEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "recipeId") val recipeId: String,
    @ColumnInfo(name = "product_name") val productName: String,
    val quantity: Float,
    val unit: String,
    @ColumnInfo(name = "is_optional") val isOptional: Boolean
)