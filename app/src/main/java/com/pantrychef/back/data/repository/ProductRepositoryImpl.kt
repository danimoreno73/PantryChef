package com.pantrychef.back.data.repository

import com.pantrychef.back.data.local.dao.ProductDao
import com.pantrychef.back.data.mock.MockProductDataSource
import com.pantrychef.back.model.Product
import com.pantrychef.back.model.enums.Category
import com.pantrychef.back.repository.ProductRepository
import kotlinx.coroutines.flow.Flow

class ProductRepositoryImpl(
    private val productDao: ProductDao,
    private val mockProductDataSource: MockProductDataSource
) : ProductRepository {
    override suspend fun getAllProducts(): Flow<List<Product>> {
        TODO("Not yet implemented")
    }

    override suspend fun getProductById(id: String): Result<Product> {
        TODO("Not yet implemented")
    }

    override suspend fun addProduct(product: Product): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun updateProduct(product: Product): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteProduct(id: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun searchProducts(query: String): Flow<List<Product>> {
        TODO("Not yet implemented")
    }

    override suspend fun getProductsByCategory(category: Category): Flow<List<Product>> {
        TODO("Not yet implemented")
    }

}
