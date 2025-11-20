package com.pantrychef.back.repository

import com.pantrychef.back.model.MealLog
import kotlinx.coroutines.flow.Flow

interface MealLogRepository {
    suspend fun logMeal(mealLog: MealLog): Result<Unit>
    suspend fun getMealHistory(startDate: Long, endDate: Long): Flow<List<MealLog>>
    suspend fun deleteMealLog(id: String): Result<Unit>
    suspend fun getMealCount(startDate: Long, endDate: Long): Result<Int>
}