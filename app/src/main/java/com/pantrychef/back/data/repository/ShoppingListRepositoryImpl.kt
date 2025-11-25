package com.pantrychef.back.data.repository

import com.pantrychef.back.data.local.dao.ShoppingItemDao
import com.pantrychef.back.data.mapper.ShoppingItemMapper
import com.pantrychef.back.model.ShoppingItem
import com.pantrychef.back.repository.ShoppingListRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ShoppingListRepositoryImpl(
    private val shoppingItemDao: ShoppingItemDao
): ShoppingListRepository {

    override suspend fun getShoppingList(): Flow<List<ShoppingItem>> {
        return shoppingItemDao.getAllItems().map { shoppingItemEntities ->
            shoppingItemEntities.map { ShoppingItemMapper.entityToModel(it) }
        }
    }

    override suspend fun addItem(item: ShoppingItem): Result<Unit> {
        return try {
            shoppingItemDao.insertItem(ShoppingItemMapper.modelToEntity(item))
            Result.success(Unit)
        }catch (e: Exception){
            Result.failure(e)
        }
    }

    override suspend fun removeItem(id: String): Result<Unit> {
        return try {
            shoppingItemDao.deleteItem(id)
            Result.success(Unit)
        }catch (e: Exception){
            Result.failure(e)
        }
    }

    override suspend fun markAsPurchased(id: String): Result<Unit> {
        return try {
            shoppingItemDao.updatePurchaseStatus(id, true)
            Result.success(Unit)
        }catch (e: Exception){
            Result.failure(e)
        }
    }

    override suspend fun unmarkAsPurchased(id: String): Result<Unit> {
        return try {
            shoppingItemDao.updatePurchaseStatus(id, false)
            Result.success(Unit)
        }catch (e: Exception){
            Result.failure(e)
        }
    }

    override suspend fun clearPurchased(): Result<Unit> {
        return try {
            shoppingItemDao.deletePurchasedItems()
            Result.success(Unit)
        }catch (e: Exception){
            Result.failure(e)
        }
    }

}
