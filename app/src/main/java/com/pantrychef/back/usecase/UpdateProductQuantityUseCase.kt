package com.pantrychef.back.usecase

import com.pantrychef.back.repository.ProductRepository

class UpdateProductQuantityUseCase(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(productId: String, newQuantity: Float): Result<Unit> {
        return try {
            val product = productRepository.getProductById(productId).getOrThrow()
            val updatedProduct = product.copy(
                quantity = newQuantity,
                updatedAt = System.currentTimeMillis()
            )
            productRepository.updateProduct(updatedProduct)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}