package com.pantrychef.back.data.local.entity

import androidx.room.ColumnInfo
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
    val location: String?,
    val brand: String?,
    @ColumnInfo(name = "updated_at") val updatedAt: Long
)
