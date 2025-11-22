package com.pantrychef.back.data.mapper

import com.pantrychef.back.data.local.entity.ProductEntity
import com.pantrychef.back.model.Product
import com.pantrychef.back.model.enums.Category
import com.pantrychef.back.model.enums.Unit

object ProductMapper {

    fun entityToModel(entity: ProductEntity): Product{
        return Product(
            id = entity.id,
            name = entity.name,
            category = Category.valueOf(entity.category),
            quantity = entity.quantity,
            unit = Unit.valueOf(entity.unit),
            lowStockThreshold = entity.lowStockThreshold,
            location = entity.location,
            brand = entity.brand,
            updatedAt = entity.updatedAt
        )
    }
    fun modelToEntity(model: Product): ProductEntity{
        return ProductEntity(
            id = model.id,
            name = model.name,
            category = model.category.name,
            quantity = model.quantity,
            unit = model.unit.name,
            lowStockThreshold = model.lowStockThreshold,
            location = model.location,
            brand = model.brand,
            updatedAt = model.updatedAt
        )
    }
}