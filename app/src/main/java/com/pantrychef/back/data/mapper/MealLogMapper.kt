package com.pantrychef.back.data.mapper

import com.pantrychef.back.data.local.entity.MealLogEntity
import com.pantrychef.back.model.MealLog
import com.pantrychef.back.model.enums.MealType

object MealLogMapper {

    fun entityToModel(entity: MealLogEntity): MealLog{
        return MealLog(
            id = entity.id,
            recipeId = entity.recipeId,
            recipeName = entity.recipeName,
            timestamp = entity.timestamp,
            mealType = MealType.valueOf(entity.mealType),
            servings = entity.servings,
            caloriesEstimate = entity.caloriesEstimate
        )
    }
    fun modelToEntity(model: MealLog): MealLogEntity{
        return MealLogEntity(
            id = model.id,
            recipeId = model.recipeId,
            recipeName = model.recipeName,
            timestamp = model.timestamp,
            mealType = model.mealType.name,
            servings = model.servings,
            caloriesEstimate = model.caloriesEstimate
        )
    }
}