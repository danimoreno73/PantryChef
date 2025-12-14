package com.pantrychef.back.data.local

import com.pantrychef.back.data.local.dao.ProductDao
import com.pantrychef.back.data.local.dao.RecipeDao
import com.pantrychef.back.data.local.entity.IngredientEntity
import com.pantrychef.back.data.local.entity.ProductEntity
import com.pantrychef.back.data.local.entity.RecipeEntity
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SeedDataHelper @Inject constructor(
    private val productDao: ProductDao,
    private val recipeDao: RecipeDao
) {

    /**
     * Inserta datos de ejemplo si la base de datos está vacía
     */
    suspend fun seedDatabaseIfEmpty() {
        // Verificar si ya hay datos
        val existingProducts = productDao.getAllProducts().first()

        if (existingProducts.isEmpty()) {
            // Primera vez que abre la app, insertar datos de ejemplo
            seedRecipes()
        }
    }

    private suspend fun seedRecipes() {
        // Receta 1: Pasta con tomate (COCINABLES - tenemos todos los ingredientes)
        val pastaRecipe = RecipeEntity(
            id = "recipe-1",
            name = "Pasta con Tomate",
            imageUrl = "https://images.unsplash.com/photo-1702650621517-927b5caa28bc?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8Mnx8cGFzdGElMjBjb24lMjB0b21hdGV8ZW58MHx8MHx8fDA%3D",
            prepTimeMinutes = 20,
            servings = 2,
            difficulty = "EASY",
            steps = """["Hervir agua con sal","Cocinar la pasta 10 minutos","Calentar el tomate en una sartén","Mezclar pasta con tomate","Servir caliente"]""",
            createdBy = "system",
            isPublic = true,
            createdAt = System.currentTimeMillis()
        )

        val pastaIngredients = listOf(
            IngredientEntity(
                id = "ing-1",
                recipeId = "recipe-1",
                productName = "Pasta",
                quantity = 200f,
                unit = "GRAMS",
                isOptional = false
            ),
            IngredientEntity(
                id = "ing-2",
                recipeId = "recipe-1",
                productName = "Tomate",
                quantity = 3f,
                unit = "UNITS",
                isOptional = false
            ),
            IngredientEntity(
                id = "ing-3",
                recipeId = "recipe-1",
                productName = "Aceite de Oliva",
                quantity = 2f,
                unit = "TABLESPOONS",
                isOptional = false
            ),
            IngredientEntity(
                id = "ing-4",
                recipeId = "recipe-1",
                productName = "Sal",
                quantity = 1f,
                unit = "TABLESPOONS",
                isOptional = false
            )
        )

        // Receta 2: Tortilla de patatas (CASI COCINABLES - falta patatas)
        val tortillaRecipe = RecipeEntity(
            id = "recipe-2",
            name = "Tortilla de Patatas",
            imageUrl = "https://images.unsplash.com/photo-1639669794539-952631b44515?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8M3x8dG9ydGlsbGF8ZW58MHx8MHx8fDA%3D",
            prepTimeMinutes = 30,
            servings = 4,
            difficulty = "MEDIUM",
            steps = """["Pelar y cortar las patatas","Freír las patatas con cebolla","Batir los huevos","Mezclar patatas con huevo","Cocinar en la sartén"]""",
            createdBy = "system",
            isPublic = true,
            createdAt = System.currentTimeMillis()
        )

        val tortillaIngredients = listOf(
            IngredientEntity(
                id = "ing-5",
                recipeId = "recipe-2",
                productName = "Huevos",
                quantity = 6f,
                unit = "UNITS",
                isOptional = false
            ),
            IngredientEntity(
                id = "ing-6",
                recipeId = "recipe-2",
                productName = "Patatas",
                quantity = 4f,
                unit = "UNITS",
                isOptional = false
            ),
            IngredientEntity(
                id = "ing-7",
                recipeId = "recipe-2",
                productName = "Cebolla",
                quantity = 1f,
                unit = "UNITS",
                isOptional = true
            ),
            IngredientEntity(
                id = "ing-8",
                recipeId = "recipe-2",
                productName = "Aceite de Oliva",
                quantity = 100f,
                unit = "MILLILITERS",
                isOptional = false
            ),
            IngredientEntity(
                id = "ing-9",
                recipeId = "recipe-2",
                productName = "Sal",
                quantity = 1f,
                unit = "TABLESPOONS",
                isOptional = false
            )
        )

        // Receta 3: Arroz con pollo (COCINABLES - tenemos todo)
        val arrozRecipe = RecipeEntity(
            id = "recipe-3",
            name = "Arroz con Pollo",
            imageUrl = "https://images.unsplash.com/photo-1569058242252-623df46b5025?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8Mnx8YXJyb3olMjB5JTIwcG9sbG98ZW58MHx8MHx8fDA%3D",
            prepTimeMinutes = 45,
            servings = 4,
            difficulty = "MEDIUM",
            steps = """["Sofreír cebolla y ajo","Añadir el pollo troceado","Agregar el arroz y remover","Añadir agua y cocinar 20 minutos","Servir caliente"]""",
            createdBy = "system",
            isPublic = true,
            createdAt = System.currentTimeMillis()
        )

        val arrozIngredients = listOf(
            IngredientEntity(
                id = "ing-10",
                recipeId = "recipe-3",
                productName = "Arroz",
                quantity = 400f,
                unit = "GRAMS",
                isOptional = false
            ),
            IngredientEntity(
                id = "ing-11",
                recipeId = "recipe-3",
                productName = "Pollo",
                quantity = 500f,
                unit = "GRAMS",
                isOptional = false
            ),
            IngredientEntity(
                id = "ing-12",
                recipeId = "recipe-3",
                productName = "Cebolla",
                quantity = 1f,
                unit = "UNITS",
                isOptional = false
            ),
            IngredientEntity(
                id = "ing-13",
                recipeId = "recipe-3",
                productName = "Ajo",
                quantity = 2f,
                unit = "UNITS",
                isOptional = false
            ),
            IngredientEntity(
                id = "ing-14",
                recipeId = "recipe-3",
                productName = "Aceite de Oliva",
                quantity = 3f,
                unit = "TABLESPOONS",
                isOptional = false
            )
        )

        // Insertar recetas e ingredientes
        recipeDao.insertRecipe(pastaRecipe)
        pastaIngredients.forEach { recipeDao.insertIngredient(it) }

        recipeDao.insertRecipe(tortillaRecipe)
        tortillaIngredients.forEach { recipeDao.insertIngredient(it) }

        recipeDao.insertRecipe(arrozRecipe)
        arrozIngredients.forEach { recipeDao.insertIngredient(it) }

    }
}