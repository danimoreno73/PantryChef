package com.pantrychef.back.repository

import com.pantrychef.back.model.ShoppingItem
import kotlinx.coroutines.flow.Flow

interface ShoppingListRepository {
    suspend fun getShoppingList(): Flow<List<ShoppingItem>>
    suspend fun addItem(item: ShoppingItem): Result<Unit>
    suspend fun removeItem(id: String): Result<Unit>
    suspend fun markAsPurchased(id: String): Result<Unit>
    suspend fun clearPurchased(): Result<Unit>
}