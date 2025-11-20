package com.pantrychef.back.repository

import com.pantrychef.back.model.Alert
import kotlinx.coroutines.flow.Flow

interface AlertRepository {
    suspend fun getAllAlerts(): Flow<List<Alert>>
    suspend fun createAlert(alert: Alert): Result<Unit>
    suspend fun dismissAlert(id: String): Result<Unit>
    suspend fun markAsResolved(id: String): Result<Unit>
}