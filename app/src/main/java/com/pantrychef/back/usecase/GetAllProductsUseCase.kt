package com.pantrychef.back.usecase

import com.pantrychef.back.model.Product
import com.pantrychef.back.repository.ProductRepository
import kotlinx.coroutines.flow.Flow

class GetAllProductsUseCase(
    private val productRepository: ProductRepository
) {
     suspend operator fun invoke(): Flow<List<Product>> {
        return productRepository.getAllProducts()
    }
}