package com.pantrychef.back.repository

import com.pantrychef.back.model.Product
import com.pantrychef.back.model.enums.Category
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    suspend fun getAllProducts(): Flow<List<Product>>
    suspend fun getProductById(id: String): Result<Product>
    suspend fun addProduct(product: Product): Result<Unit>
    suspend fun updateProduct(product: Product): Result<Unit>
    suspend fun deleteProduct(id: String): Result<Unit>
    suspend fun searchProducts(query: String): Flow<List<Product>>
    suspend fun getProductsByCategory(category: Category): Flow<List<Product>>
}