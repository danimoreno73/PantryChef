package com.pantrychef.back.data.mapper

import com.pantrychef.back.data.local.entity.IngredientEntity
import com.pantrychef.back.data.local.entity.RecipeEntity
import com.pantrychef.back.data.local.entity.RecipeWithIngredients
import com.pantrychef.back.model.Ingredient
import com.pantrychef.back.model.Recipe
import com.pantrychef.back.model.enums.Difficulty
import com.pantrychef.back.model.enums.Unit
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object RecipeMapper {

    private val jsonFormatter = Json

    fun entityToModel(recipeWithIngredients: RecipeWithIngredients): Recipe{
        val entity = recipeWithIngredients.recipe
        val ingredientEntities = recipeWithIngredients.ingredients

        return Recipe(
            id = entity.id,
            name = entity.name,
            imageUrl = entity.imageUrl,
            prepTimeMinutes = entity.prepTimeMinutes,
            servings = entity.servings,
            difficulty = Difficulty.valueOf(entity.difficulty),
            steps = parseStepsFromJson(entity.steps),
            ingredients = ingredientEntities.map { ingredientEntityToModel(it) },
            createdBy = entity.createdBy,
            isPublic = entity.isPublic,
            createdAt = entity.createdAt
        )
    }

    fun modelToEntity(model: Recipe): Pair<RecipeEntity, List<IngredientEntity>>{
        val recipeEntity = RecipeEntity(
            id = model.id,
            name = model.name,
            imageUrl = model.imageUrl,
            prepTimeMinutes = model.prepTimeMinutes,
            servings = model.servings,
            difficulty = model.difficulty.name,
            steps = serializeStepsToJson(model.steps),
            createdBy = model.createdBy,
            isPublic = model.isPublic,
            createdAt = model.createdAt
        )
        val ingredientsEntities = model.ingredients.map { ingredientModelToEntity(it) }
        return Pair(recipeEntity, ingredientsEntities)
    }

    private fun serializeStepsToJson(steps: List<String>): String {
        return jsonFormatter.encodeToString(steps)
    }

    private fun parseStepsFromJson(json: String): List<String> {
        if (json.isBlank()) return emptyList()
        return jsonFormatter.decodeFromString(json)
    }
    private fun ingredientModelToEntity(model: Ingredient): IngredientEntity {
        return IngredientEntity(
            id = model.id,
            recipeId = model.recipeId,
            productName = model.productName,
            quantity = model.quantity,
            unit = model.unit.name,
            isOptional = model.isOptional
        )
    }

    private fun ingredientEntityToModel(entity: IngredientEntity): Ingredient {
        return Ingredient(
            id = entity.id,
            recipeId = entity.recipeId,
            productName = entity.productName,
            quantity = entity.quantity,
            unit = Unit.valueOf(entity.unit),
            isOptional = entity.isOptional
        )
    }

}


