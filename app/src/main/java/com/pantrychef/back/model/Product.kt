package com.pantrychef.back.model

import com.pantrychef.back.model.enums.Category
import com.pantrychef.back.model.enums.Unit

data class Product(
    val id: String,
    val name: String,
    val category: Category,
    val quantity: Float,
    val unit: Unit,
    val lowStockThreshold: Float,
    val location: String? = null,
    val brand: String? = null,
    val updatedAt: Long
)