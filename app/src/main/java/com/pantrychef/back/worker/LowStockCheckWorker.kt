package com.pantrychef.back.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pantrychef.back.repository.ProductRepository
import com.pantrychef.back.utils.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class LowStockCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val productRepository: ProductRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val products = productRepository.getAllProducts().first()

            val urgentProducts = products.filter { product ->
                product.quantity <= product.lowStockThreshold
            }

            if (urgentProducts.isNotEmpty()) {
                val notificationHelper = NotificationHelper(applicationContext)
                notificationHelper.showLowStockAlert(urgentProducts.size)
            }

            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("LowStockCheckWorker", "Error", e)
            Result.failure()
        }
    }
}