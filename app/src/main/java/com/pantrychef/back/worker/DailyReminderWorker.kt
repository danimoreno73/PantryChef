package com.pantrychef.back.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pantrychef.back.utils.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class DailyReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val notificationHelper = NotificationHelper(applicationContext)
        notificationHelper.showDailyReminder(
            title = "Registra tu comida de hoy",
            message = "¿Qué has cocinado hoy? Mantén tu despensa actualizada"
        )
        return Result.success()
    }
}