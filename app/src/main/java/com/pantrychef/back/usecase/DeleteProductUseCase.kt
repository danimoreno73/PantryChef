package com.pantrychef.back.usecase

import com.pantrychef.back.repository.ProductRepository

class DeleteProductUseCase(
    private val productRepository: ProductRepository
) {

    suspend operator fun invoke(
        id: String
    ): Result<Unit>{
        return try {
            productRepository.deleteProduct(id)
            Result.success(Unit)
        }catch (e: Exception){
            Result.failure(e)
        }
    }
}