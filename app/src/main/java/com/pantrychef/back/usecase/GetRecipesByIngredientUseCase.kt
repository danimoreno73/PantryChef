package com.pantrychef.back.usecase

import com.pantrychef.back.model.Product
import com.pantrychef.back.model.Recipe
import com.pantrychef.back.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow

import kotlinx.coroutines.flow.map

class GetRecipesByIngredientUseCase(
    private val recipeRepository: RecipeRepository
) {
    suspend operator fun invoke(product: Product): Flow<List<Recipe>> {
        return recipeRepository.getAllRecipes()
            .map { recipes ->
                recipes.filter { recipe ->
                    recipe.ingredients.any { ingredient ->
                        ingredient.productName == product.name
                    }
                }
            }
    }
}