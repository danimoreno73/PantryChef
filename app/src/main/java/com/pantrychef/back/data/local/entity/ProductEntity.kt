package com.pantrychef.back.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val quantity: Float,
    val unit: String,
    val lowStockThreshold: Float,
    val location: String? = null,
    val brand: String? = null,
    val updatedAt: Long
)
