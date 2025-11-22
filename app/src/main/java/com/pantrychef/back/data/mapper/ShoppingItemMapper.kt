package com.pantrychef.back.data.mapper


import com.pantrychef.back.data.local.entity.ShoppingItemEntity
import com.pantrychef.back.model.ShoppingItem
import com.pantrychef.back.model.enums.Source
import com.pantrychef.back.model.enums.Unit

object ShoppingItemMapper {

    fun entityToModel(entity: ShoppingItemEntity): ShoppingItem{
        return ShoppingItem(
            id = entity.id,
            productName = entity.productName,
            quantity = entity.quantity,
            unit = Unit.valueOf(entity.unit),
            source = Source.valueOf(entity.source),
            linkedRecipeId = entity.linkedRecipeId,
            isPurchased = entity.isPurchased,
            priority = entity.priority
        )
    }
    fun modelToEntity(model: ShoppingItem): ShoppingItemEntity{
        return ShoppingItemEntity(
            id = model.id,
            productName = model.productName,
            quantity = model.quantity,
            unit = model.unit.name,
            source = model.source.name,
            linkedRecipeId = model.linkedRecipeId,
            isPurchased = model.isPurchased,
            priority = model.priority
        )
    }
}