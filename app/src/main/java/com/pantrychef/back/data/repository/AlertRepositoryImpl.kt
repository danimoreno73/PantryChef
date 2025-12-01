package com.pantrychef.back.data.repository

import com.pantrychef.back.data.local.dao.AlertDao
import com.pantrychef.back.data.mapper.AlertMapper
import com.pantrychef.back.model.Alert
import com.pantrychef.back.repository.AlertRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AlertRepositoryImpl(
    private val alertDao: AlertDao
): AlertRepository{


    override fun getAllAlerts(): Flow<List<Alert>> {
        return alertDao.getAllAlerts().map { alertsList ->
            alertsList.map { alertEntity -> AlertMapper.entityToModel(alertEntity) }
        }
    }

    override suspend fun createAlert(alert: Alert): Result<Unit> {
        return try {
            val alertEntity = AlertMapper.modelToEntity(alert)
            alertDao.insertAlert(alertEntity)
            Result.success(Unit)
        }catch (e: Exception){
            Result.failure(e)
        }
    }

    override suspend fun dismissAlert(id: String): Result<Unit> {
        return try {
            alertDao.deleteAlert(id)
            Result.success(Unit)
        }catch (e: Exception){
            Result.failure(e)
        }
    }

    override suspend fun markAsResolved(id: String): Result<Unit> {
        return try {
            alertDao.markAsResolved(id)
            Result.success(Unit)
        }catch (e: Exception){
            Result.failure(e)
        }
    }

}
