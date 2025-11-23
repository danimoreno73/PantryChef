package com.pantrychef.back.data.repository

import com.pantrychef.back.data.local.dao.AlertDao
import com.pantrychef.back.model.Alert
import com.pantrychef.back.repository.AlertRepository
import kotlinx.coroutines.flow.Flow

class AlertRepositoryImpl(alertDao: AlertDao): AlertRepository{
    override suspend fun getAllAlerts(): Flow<List<Alert>> {
        TODO("Not yet implemented")
    }

    override suspend fun createAlert(alert: Alert): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun dismissAlert(id: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun markAsResolved(id: String): Result<Unit> {
        TODO("Not yet implemented")
    }

}
