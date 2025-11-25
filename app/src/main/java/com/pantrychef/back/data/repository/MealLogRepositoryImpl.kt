package com.pantrychef.back.data.repository

import com.pantrychef.back.data.local.dao.MealLogDao
import com.pantrychef.back.data.local.entity.MealLogEntity
import com.pantrychef.back.data.mapper.MealLogMapper
import com.pantrychef.back.model.MealLog
import com.pantrychef.back.repository.MealLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MealLogRepositoryImpl(
    private val mealLogDao: MealLogDao
): MealLogRepository {
    override suspend fun logMeal(mealLog: MealLog): Result<Unit> {
        return try {
            val entity : MealLogEntity = MealLogMapper.modelToEntity(mealLog)
            mealLogDao.insertMealLog(entity)
            Result.success(Unit)
        }catch (e: Exception){
            Result.failure(e)
        }
    }

    override suspend fun getMealHistory(
        startDate: Long,
        endDate: Long
    ): Flow<List<MealLog>> {
        return mealLogDao.getMealHistory(startDate, endDate).map { mealLogEntities ->
            mealLogEntities.map { MealLogMapper.entityToModel(it) }
        }
    }

    override suspend fun deleteMealLog(id: String): Result<Unit> {
       return try {
           mealLogDao.deleteMealLog(id)
           Result.success(Unit)
       }catch (e: Exception){
           Result.failure(e)
       }
    }

    override suspend fun getMealCount(
        startDate: Long,
        endDate: Long
    ): Result<Int> {
        return try {
            Result.success(mealLogDao.getMealCount(startDate, endDate))
        }catch (e: Exception){
            Result.failure(e)
        }
    }

}
