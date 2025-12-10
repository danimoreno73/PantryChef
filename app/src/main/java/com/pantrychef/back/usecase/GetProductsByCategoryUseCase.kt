package com.pantrychef.back.usecase

import com.pantrychef.back.model.Product
import com.pantrychef.back.model.enums.Category
import com.pantrychef.back.repository.ProductRepository
import kotlinx.coroutines.flow.Flow

class GetProductsByCategoryUseCase(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(
        category: Category
    ): Flow<List<Product>>{
            return productRepository.getProductsByCategory(category)
    }
}