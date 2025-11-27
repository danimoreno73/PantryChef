package com.pantrychef.back.usecase

import com.pantrychef.back.model.Alert
import com.pantrychef.back.model.enums.AlertType
import com.pantrychef.back.model.enums.Severity
import com.pantrychef.back.repository.AlertRepository
import com.pantrychef.back.repository.ProductRepository
import kotlinx.coroutines.flow.first

class ComputeLowStockAlertsUseCase(
    private val productRepository: ProductRepository,
    private val alertRepository: AlertRepository
) {

    suspend operator fun invoke(): Result<List<Alert>>{
        return try {
            productRepository.getAllProducts().first().let { products ->
                val alerts = products.filter { product ->
                    product.quantity <= product.lowStockThreshold
                }.map { product ->
                    val severity = when{
                        product.quantity == 0f -> Severity.URGENT
                        product.quantity <= product.lowStockThreshold * 0.5f -> Severity.RESTOCK
                        else -> Severity.LOW
                    }
                    val alertType = if (product.quantity == 0f) AlertType.OUT_OF_STOCK else AlertType.LOW_STOCK
                    val message =  when (severity) {
                        Severity.URGENT -> "Out of Stock of ${product.name}"
                        Severity.RESTOCK -> "Time to buy ${(product.lowStockThreshold - product.quantity).toInt()} ${product.unit.name.lowercase()} of ${product.name}"
                        Severity.LOW -> "Remaining ${product.quantity.toInt()} ${product.unit.name.lowercase()} of ${product.name}"
                    }
                    Alert(
                        id = "alert-${product.id}-${System.currentTimeMillis()}",
                        productId = product.id,
                        productName = product.name,
                        alertType = alertType ,
                        severity = severity,
                        message = message,
                        createdAt = System.currentTimeMillis(),
                        isResolved = false
                    )
                }
                alerts.forEach { alert -> alertRepository.createAlert(alert) }
                Result.success(alerts)
            }

        }catch (e: Exception){
            Result.failure(e)
        }
    }
}