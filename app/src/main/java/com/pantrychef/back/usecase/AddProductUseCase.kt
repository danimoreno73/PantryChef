package com.pantrychef.back.usecase

import com.pantrychef.back.model.Product
import com.pantrychef.back.model.enums.Category
import com.pantrychef.back.repository.ProductRepository
import java.util.UUID

class AddProductUseCase(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(
        name: String,
        category: Category,
        quantity: Float,
        unit: com.pantrychef.back.model.enums.Unit,
        lowStockThreshold: Float,
        location: String? = null,
        brand: String? = null
    ): Result<Unit> {
        return try {
            val product = Product(
                id = "prod-${UUID.randomUUID()}",
                name = name,
                category = category,
                quantity = quantity,
                unit = unit,
                lowStockThreshold = lowStockThreshold,
                location = location,
                brand = brand,
                updatedAt = System.currentTimeMillis()
            )

            productRepository.addProduct(product)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}