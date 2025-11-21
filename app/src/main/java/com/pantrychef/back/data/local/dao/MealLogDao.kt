package com.pantrychef.back.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pantrychef.back.data.local.entity.MealLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealLog(mealLog: MealLogEntity)

    @Query("SELECT * FROM meal_logs WHERE timestamp BETWEEN :startDate AND :endDate ORDER BY timestamp DESC")
    fun getMealHistory(startDate: Long, endDate: Long): Flow<List<MealLogEntity>>

    @Query("DELETE FROM meal_logs WHERE id = :id")
    suspend fun deleteMealLog(id: String)

    @Query("SELECT COUNT(*) FROM meal_logs WHERE timestamp BETWEEN :startDate AND :endDate")
    suspend fun getMealCount(startDate: Long, endDate: Long): Int
}