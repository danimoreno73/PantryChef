package com.pantrychef.back.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.pantrychef.back.data.local.entity.IngredientEntity
import com.pantrychef.back.data.local.entity.RecipeEntity
import com.pantrychef.back.data.local.entity.RecipeWithIngredients
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Transaction
    @Query("SELECT * FROM recipes ORDER BY name ASC")
    fun getAllRecipesWithIngredients(): Flow<List<RecipeWithIngredients>>

    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getRecipeById(id: String): RecipeWithIngredients?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: RecipeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredients(ingredients: List<IngredientEntity>)

    @Update
    suspend fun updateRecipe(recipe: RecipeEntity)

    @Query("DELETE FROM recipes WHERE id = :id")
    suspend fun deleteRecipe(id: String)

    @Query("DELETE FROM ingredients WHERE recipeId = :recipeId")
    suspend fun deleteIngredientsByRecipe(recipeId: String)

    // Cambiado de query a Transaction y devolvemos con ingredientes para no perder la relación con los ingredientes
    @Transaction
    @Query("SELECT * FROM recipes WHERE created_by = :userId ORDER BY name ASC")
    fun getUserRecipes(userId: String): Flow<List<RecipeWithIngredients>>

    @Transaction
    @Query("""
        SELECT DISTINCT recipes.* FROM recipes 
        LEFT JOIN ingredients ON recipes.id = ingredients.recipeId 
        WHERE recipes.name LIKE '%' || :query || '%' 
        OR ingredients.product_name LIKE '%' || :query || '%'
        ORDER BY recipes.name ASC
    """)
    fun searchRecipes(query: String): Flow<List<RecipeWithIngredients>>
}