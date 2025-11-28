package com.pantrychef.back.repository

import com.pantrychef.back.model.Recipe
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    suspend fun getAllRecipes(): Flow<List<Recipe>>
    suspend fun getRecipeById(id: String): Result<Recipe>
    suspend fun createRecipe(recipe: Recipe): Result<Unit>
    suspend fun updateRecipe(recipe: Recipe): Result<Unit>
    suspend fun deleteRecipe(id: String): Result<Unit>
    suspend fun getUserRecipes(userId: String): Flow<List<Recipe>>
    suspend fun searchRecipes(query: String): Flow<List<Recipe>>
}