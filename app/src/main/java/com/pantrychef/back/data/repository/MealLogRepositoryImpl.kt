package com.pantrychef.back.data.repository

import com.pantrychef.back.data.local.dao.MealLogDao
import com.pantrychef.back.model.MealLog
import com.pantrychef.back.repository.MealLogRepository
import kotlinx.coroutines.flow.Flow

class MealLogRepositoryImpl(mealLogDao: MealLogDao): MealLogRepository {
    override suspend fun logMeal(mealLog: MealLog): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun getMealHistory(
        startDate: Long,
        endDate: Long
    ): Flow<List<MealLog>> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteMealLog(id: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun getMealCount(
        startDate: Long,
        endDate: Long
    ): Result<Int> {
        TODO("Not yet implemented")
    }

}
