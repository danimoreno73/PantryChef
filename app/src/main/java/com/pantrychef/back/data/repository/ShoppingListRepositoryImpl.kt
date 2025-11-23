package com.pantrychef.back.data.repository

import com.pantrychef.back.data.local.dao.ShoppingItemDao
import com.pantrychef.back.model.ShoppingItem
import com.pantrychef.back.repository.ShoppingListRepository
import kotlinx.coroutines.flow.Flow

class ShoppingListRepositoryImpl(shoppingItemDao: ShoppingItemDao): ShoppingListRepository {
    override suspend fun getShoppingList(): Flow<List<ShoppingItem>> {
        TODO("Not yet implemented")
    }

    override suspend fun addItem(item: ShoppingItem): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun removeItem(id: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun markAsPurchased(id: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun clearPurchased(): Result<Unit> {
        TODO("Not yet implemented")
    }

}
