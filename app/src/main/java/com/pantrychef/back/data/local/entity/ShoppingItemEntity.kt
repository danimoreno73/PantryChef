package com.pantrychef.back.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_items")
data class ShoppingItemEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "product_name") val productName: String,
    val quantity: Float,
    val unit: String,
    val source: String,
    @ColumnInfo(name = "linked_recipe_id") val linkedRecipeId: String?,
    @ColumnInfo(name = "is_purchased") val isPurchased: Boolean,
    val priority: Int
)