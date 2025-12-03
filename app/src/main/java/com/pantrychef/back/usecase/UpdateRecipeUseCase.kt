package com.pantrychef.back.usecase

import com.pantrychef.back.model.Recipe
import com.pantrychef.back.repository.ProductRepository
import com.pantrychef.back.repository.RecipeRepository

class UpdateRecipeUseCase(
    private val recipeRepository: RecipeRepository
) {

    suspend operator fun invoke(
        recipe: Recipe
    ): Result<Unit>{
        return try {
            recipeRepository.updateRecipe(recipe)
            Result.success(Unit)
        }catch (e: Exception){
            Result.failure(e)
        }
    }
}