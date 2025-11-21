package com.pantrychef.back.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pantrychef.back.data.local.entity.ShoppingItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingItemDao {
    @Query("SELECT * FROM shopping_items ORDER BY priority DESC, product_name ASC")
    fun getAllItems(): Flow<List<ShoppingItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ShoppingItemEntity)

    @Query("DELETE FROM shopping_items WHERE id = :id")
    suspend fun deleteItem(id: String)

    @Query("UPDATE shopping_items SET is_purchased = :isPurchased WHERE id = :id")
    suspend fun updatePurchaseStatus(id: String, isPurchased: Boolean)

    @Query("DELETE FROM shopping_items WHERE is_purchased = 1")
    suspend fun deletePurchasedItems()
}
