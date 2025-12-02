package com.pantrychef.back.usecase

import com.pantrychef.back.model.Ingredient
import com.pantrychef.back.model.Recipe
import com.pantrychef.back.model.enums.Difficulty
import com.pantrychef.back.repository.RecipeRepository

class AddRecipeUseCase(
    private val recipeRepository: RecipeRepository
) {
    suspend operator fun invoke(
        id: String,
        name: String,
        imageUrl: String? = null,
        prepTimeMinutes: Int,
        servings: Int,
        difficulty: Difficulty,
        steps: List<String>,
        ingredients: List<Ingredient>,
        createdBy: String,
        isPublic: Boolean = false,
        createdAt: Long
    ): Result<Unit>{
        return try {
            val recipe = Recipe(
                id = id,
                name = name,
                imageUrl = imageUrl,
                prepTimeMinutes = prepTimeMinutes,
                servings = servings,
                difficulty = difficulty,
                steps = steps,
                ingredients = ingredients,
                createdBy = createdBy,
                isPublic = isPublic,
                createdAt = createdAt
            )
            recipeRepository.createRecipe(recipe)
            Result.success(Unit)
        }catch (e: Exception){
            Result.failure(e)
        }
    }
}