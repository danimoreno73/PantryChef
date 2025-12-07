package com.pantrychef.back.data.repository

import com.pantrychef.back.data.local.dao.ProductDao
import com.pantrychef.back.data.mapper.ProductMapper
import com.pantrychef.back.data.mock.MockProductDataSource
import com.pantrychef.back.model.Product
import com.pantrychef.back.model.enums.Category
import com.pantrychef.back.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProductRepositoryImpl(
    private val productDao: ProductDao,
) : ProductRepository {



    override suspend fun getAllProducts(): Flow<List<Product>> {
        return productDao.getAllProducts().map { entities ->
            entities.map { ProductMapper.entityToModel(it) }
        }
    }

    override suspend fun getProductById(id: String): Result<Product> {
        return try {
            val entity = productDao.getProductById(id)
            if (entity != null) {
                Result.success(ProductMapper.entityToModel(entity))
            } else {
                Result.failure(Exception("Product not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addProduct(product: Product): Result<Unit> {
        return try {
            val entity = ProductMapper.modelToEntity(product)
            productDao.insertProduct(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProduct(product: Product): Result<Unit> {
        return try {
            val entity = ProductMapper.modelToEntity(product)
            productDao.updateProduct(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteProduct(id: String): Result<Unit> {
        return try {
            productDao.deleteProduct(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchProducts(query: String): Flow<List<Product>> {
        return productDao.searchProducts(query).map { entities ->
            entities.map { ProductMapper.entityToModel(it) }
        }
    }

    override suspend fun getProductsByCategory(category: Category): Flow<List<Product>> {
        return productDao.getProductsByCategory(category.name).map { entities ->
            entities.map { ProductMapper.entityToModel(it) }
        }
    }

}