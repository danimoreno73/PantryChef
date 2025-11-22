package com.pantrychef.back.model

import com.pantrychef.back.model.enums.Source
import com.pantrychef.back.model.enums.Unit

data class ShoppingItem(
    val id: String,
    val productName: String,
    val quantity: Float,
    val unit: Unit,
    val source: Source,
    val linkedRecipeId: String? = null,
    val isPurchased: Boolean = false,
    val priority: Int = 0
)