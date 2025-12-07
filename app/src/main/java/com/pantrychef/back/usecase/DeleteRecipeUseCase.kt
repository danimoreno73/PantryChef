package com.pantrychef.back.usecase


import com.pantrychef.back.repository.RecipeRepository

class DeleteRecipeUseCase(
    private val recipeRepository: RecipeRepository
) {

    suspend operator fun invoke(
         id: String
    ): Result<Unit> {
        return try {
            recipeRepository.deleteRecipe(id)
        }catch (e: Exception){
            Result.failure(e)
        }
    }
}