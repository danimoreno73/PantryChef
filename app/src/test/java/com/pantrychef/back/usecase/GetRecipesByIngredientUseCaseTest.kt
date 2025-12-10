package com.pantrychef.back.usecase

import app.cash.turbine.test
import com.pantrychef.back.model.Ingredient
import com.pantrychef.back.model.Product
import com.pantrychef.back.model.Recipe
import com.pantrychef.back.model.enums.Category
import com.pantrychef.back.model.enums.Difficulty
import com.pantrychef.back.model.enums.Unit
import com.pantrychef.back.repository.RecipeRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetRecipesByIngredientUseCaseTest {

    // Mocks
    private lateinit var recipeRepository: RecipeRepository

    // System under test
    private lateinit var useCase: GetRecipesByIngredientUseCase

    @Before
    fun setup() {
        recipeRepository = mockk()
        useCase = GetRecipesByIngredientUseCase(recipeRepository)
    }

    @Test
    fun `invoke returns recipes containing the product as ingredient`() = runTest {
        // Given
        val product = Product(
            id = "1",
            name = "Tomate",
            category = Category.VEGETABLES,
            quantity = 5f,
            unit = Unit.UNITS,
            lowStockThreshold = 2f,
            location = null,
            brand = null,
            updatedAt = System.currentTimeMillis()
        )

        // Receta que SÍ tiene Tomate
        val recipeMatch = Recipe(
            id = "1",
            name = "Ensalada de Tomate",
            imageUrl = null,
            prepTimeMinutes = 10,
            servings = 2,
            difficulty = Difficulty.EASY,
            steps = listOf("Cortar tomate"),
            ingredients = listOf(
                Ingredient("i1", "1", "Tomate", 2f, Unit.UNITS, false),
                Ingredient("i2", "1", "Aceite", 10f, Unit.MILLILITERS, false)
            ),
            createdBy = "user1",
            isPublic = false,
            createdAt = System.currentTimeMillis()
        )

        // Receta que NO tiene Tomate
        val recipeNoMatch = Recipe(
            id = "2",
            name = "Pasta Blanca",
            imageUrl = null,
            prepTimeMinutes = 15,
            servings = 1,
            difficulty = Difficulty.EASY,
            steps = listOf("Hervir pasta"),
            ingredients = listOf(
                Ingredient("i3", "2", "Pasta", 100f, Unit.GRAMS, false),
                Ingredient("i4", "2", "Queso", 50f, Unit.GRAMS, false)
            ),
            createdBy = "user1",
            isPublic = false,
            createdAt = System.currentTimeMillis()
        )

        coEvery { recipeRepository.getAllRecipes() } returns flowOf(listOf(recipeMatch, recipeNoMatch))

        // When & Then
        useCase(product).test {
            val result = awaitItem()

            // Verificamos que solo devolvió la receta que coincide
            assertEquals(1, result.size)
            assertEquals("Ensalada de Tomate", result[0].name)

            awaitComplete()
        }
    }

    @Test
    fun `invoke returns empty list when no recipe contains the product`() = runTest {
        // Given
        val product = Product(
            id = "99",
            name = "Trufa", // Ingrediente raro que no está en ninguna receta
            category = Category.OTHERS,
            quantity = 1f,
            unit = Unit.UNITS,
            lowStockThreshold = 0f,
            location = null,
            brand = null,
            updatedAt = System.currentTimeMillis()
        )

        val recipe = Recipe(
            id = "1",
            name = "Tortilla",
            imageUrl = null,
            prepTimeMinutes = 10,
            servings = 2,
            difficulty = Difficulty.EASY,
            steps = listOf("Batir"),
            ingredients = listOf(
                Ingredient("i1", "1", "Huevos", 2f, Unit.UNITS, false)
            ),
            createdBy = "user1",
            isPublic = false,
            createdAt = System.currentTimeMillis()
        )

        coEvery { recipeRepository.getAllRecipes() } returns flowOf(listOf(recipe))

        // When & Then
        useCase(product).test {
            val result = awaitItem()

            assertTrue(result.isEmpty())

            awaitComplete()
        }
    }

    @Test
    fun `invoke is case sensitive correctly`() = runTest {
        // Given: Probamos si tu lógica requiere coincidencia exacta de nombre
        val product = Product(
            id = "1",
            name = "tomate", // Minúscula
            category = Category.VEGETABLES,
            quantity = 1f,
            unit = Unit.UNITS,
            lowStockThreshold = 1f,
            location = null,
            brand = null,
            updatedAt = System.currentTimeMillis()
        )

        val recipe = Recipe(
            id = "1",
            name = "Salsa",
            imageUrl = null,
            prepTimeMinutes = 5,
            servings = 4,
            difficulty = Difficulty.EASY,
            steps = listOf("Mezclar"),
            ingredients = listOf(
                Ingredient("i1", "1", "Tomate", 2f, Unit.UNITS, false) // Mayúscula
            ),
            createdBy = "user1",
            isPublic = false,
            createdAt = System.currentTimeMillis()
        )

        coEvery { recipeRepository.getAllRecipes() } returns flowOf(listOf(recipe))

        // When & Then
        useCase(product).test {
            val result = awaitItem()
            assertTrue(result.isEmpty())

            awaitComplete()
        }
    }
}