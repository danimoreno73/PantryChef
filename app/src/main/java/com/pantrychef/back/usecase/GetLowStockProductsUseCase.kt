package com.pantrychef.back.usecase

import com.pantrychef.back.model.Product
import com.pantrychef.back.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetLowStockProductsUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(): Flow<List<Product>> {
        return productRepository.getAllProducts().map { products ->
            products.filter { product ->
                product.quantity <= product.lowStockThreshold
            }
        }
    }
}