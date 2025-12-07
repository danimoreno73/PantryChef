package com.pantrychef

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.pantrychef.back.worker.DailyReminderWorker
import com.pantrychef.back.worker.LowStockCheckWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class PantryChefApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()

        android.util.Log.d("PantryChefApp", "onCreate() - Inicializando WorkManager")

        WorkManager.initialize(this, workManagerConfiguration)

        android.util.Log.d("PantryChefApp", "WorkManager inicializado con HiltWorkerFactory")

        scheduleWorkers()

        android.util.Log.d("PantryChefApp", "Workers programados")
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.DEBUG)  // ← Para ver logs
            .build()

    private fun scheduleWorkers() {
        val workManager = WorkManager.getInstance(this)

        // Recordatorio diario (cada 24 horas)
        val dailyReminderRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(
            24, TimeUnit.SECONDS
        ).build()

        workManager.enqueueUniquePeriodicWork(
            "daily_reminder",
            ExistingPeriodicWorkPolicy.KEEP,
            dailyReminderRequest
        )

        // Check de bajo stock (cada 12 horas)
        val lowStockCheckRequest = PeriodicWorkRequestBuilder<LowStockCheckWorker>(
            12, TimeUnit.SECONDS
        ).build()

        workManager.enqueueUniquePeriodicWork(
            "low_stock_check",
            ExistingPeriodicWorkPolicy.KEEP,
            lowStockCheckRequest
        )
    }
}