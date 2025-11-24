package com.pantrychef.back.data.repository

import com.pantrychef.back.data.local.dao.RecipeDao
import com.pantrychef.back.data.mapper.RecipeMapper
import com.pantrychef.back.data.mock.MockRecipeDataSource
import com.pantrychef.back.model.Recipe
import com.pantrychef.back.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RecipeRepositoryImpl(
    private val recipeDao: RecipeDao,
    private val mockRecipeDataSource: MockRecipeDataSource
) : RecipeRepository {

    private var isInitialized = false

    override suspend fun getAllRecipes(): Flow<List<Recipe>> {
        if (!isInitialized) {
            initializeMockData()
        }

        return recipeDao.getAllRecipesWithIngredients().map { recipeWithIngredientsList ->
            recipeWithIngredientsList.map { RecipeMapper.entityToModel(it) }
        }
    }

    override suspend fun getRecipeById(id: String): Result<Recipe> {
        return try {
            val recipeWithIngredients = recipeDao.getRecipeById(id)
            if (recipeWithIngredients != null) {
                Result.success(RecipeMapper.entityToModel(recipeWithIngredients))
            } else {
                Result.failure(Exception("Recipe not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createRecipe(recipe: Recipe): Result<Unit> {
        return try {
            val (recipeEntity, ingredientEntities) = RecipeMapper.modelToEntity(recipe)
            recipeDao.insertRecipe(recipeEntity)
            recipeDao.insertIngredients(ingredientEntities)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateRecipe(recipe: Recipe): Result<Unit> {
        return try {
            val (recipeEntity, ingredientEntities) = RecipeMapper.modelToEntity(recipe)
            recipeDao.updateRecipe(recipeEntity)
            recipeDao.deleteIngredientsByRecipe(recipe.id)
            recipeDao.insertIngredients(ingredientEntities)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteRecipe(id: String): Result<Unit> {
        return try {
            recipeDao.deleteRecipe(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // TODO("Implementar funcion")
    override suspend fun getUserRecipes(userId: String): Flow<List<Recipe>> {
        return recipeDao.getUserRecipes(userId).map { entities ->
            // Necesitamos obtener ingredientes para cada receta
            // Por simplicidad, retornar lista vacía aquí o hacer query adicional
            emptyList()
        }
    }

    private suspend fun initializeMockData() {
        val mockRecipes = mockRecipeDataSource.loadMockRecipes()
        mockRecipes.forEach { recipe ->
            val (recipeEntity, ingredientEntities) = RecipeMapper.modelToEntity(recipe)
            recipeDao.insertRecipe(recipeEntity)
            recipeDao.insertIngredients(ingredientEntities)
        }
        isInitialized = true
    }
}
