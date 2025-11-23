package com.pantrychef.back.data.repository

import com.pantrychef.back.data.local.dao.RecipeDao
import com.pantrychef.back.data.mock.MockRecipeDataSource
import com.pantrychef.back.model.Recipe
import com.pantrychef.back.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow

class RecipeRepositoryImpl(
    private val recipeDao: RecipeDao,
    private val mockRecipeDataSource: MockRecipeDataSource
) : RecipeRepository {
    override suspend fun getAllRecipes(): Flow<List<Recipe>> {
        TODO("Not yet implemented")
    }

    override suspend fun getRecipeById(id: String): Result<Recipe> {
        TODO("Not yet implemented")
    }

    override suspend fun createRecipe(recipe: Recipe): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun updateRecipe(recipe: Recipe): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteRecipe(id: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun getUserRecipes(userId: String): Flow<List<Recipe>> {
        TODO("Not yet implemented")
    }

}
