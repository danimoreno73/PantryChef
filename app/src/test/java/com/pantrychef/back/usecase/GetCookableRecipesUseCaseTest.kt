package com.pantrychef.back.usecase

import app.cash.turbine.test
import com.pantrychef.back.model.Ingredient
import com.pantrychef.back.model.Product
import com.pantrychef.back.model.Recipe
import com.pantrychef.back.model.enums.Category
import com.pantrychef.back.model.enums.Difficulty
import com.pantrychef.back.model.enums.Unit
import com.pantrychef.back.repository.ProductRepository
import com.pantrychef.back.repository.RecipeRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GetCookableRecipesUseCaseTest {

    // Mocks
    private lateinit var recipeRepository: RecipeRepository
    private lateinit var productRepository: ProductRepository

    // System under test
    private lateinit var useCase: GetCookableRecipesUseCase

    @Before
    fun setup() {
        recipeRepository = mockk()
        productRepository = mockk()
        useCase = GetCookableRecipesUseCase(recipeRepository, productRepository)
    }

    @Test
    fun `invoke returns only cookable recipes when all ingredients available`() = runTest {
        // Given
        val product1 = Product(
            id = "1",
            name = "Pasta",
            category = Category.GRAINS,
            quantity = 500f,
            unit = Unit.GRAMS,
            lowStockThreshold = 200f,
            location = null,
            brand = null,
            updatedAt = System.currentTimeMillis()
        )

        val product2 = Product(
            id = "2",
            name = "Tomate",
            category = Category.VEGETABLES,
            quantity = 3f,
            unit = Unit.UNITS,
            lowStockThreshold = 1f,
            location = null,
            brand = null,
            updatedAt = System.currentTimeMillis()
        )

        val cookableRecipe = Recipe(
            id = "1",
            name = "Pasta con tomate",
            imageUrl = null,
            prepTimeMinutes = 20,
            servings = 2,
            difficulty = Difficulty.EASY,
            steps = listOf("Cocinar pasta", "Añadir tomate"),
            ingredients = listOf(
                Ingredient("i1", "1", "Pasta", 200f, Unit.GRAMS, false),
                Ingredient("i2", "1", "Tomate", 2f, Unit.UNITS, false)
            ),
            createdBy = "user1",
            isPublic = false,
            createdAt = System.currentTimeMillis()
        )

        val uncookableRecipe = Recipe(
            id = "2",
            name = "Pizza",
            imageUrl = null,
            prepTimeMinutes = 30,
            servings = 4,
            difficulty = Difficulty.MEDIUM,
            steps = listOf("Hacer masa", "Hornear"),
            ingredients = listOf(
                Ingredient("i3", "2", "Harina", 300f, Unit.GRAMS, false),
                Ingredient("i4", "2", "Queso", 100f, Unit.GRAMS, false)
            ),
            createdBy = "user1",
            isPublic = false,
            createdAt = System.currentTimeMillis()
        )

        // Mock repositories
        coEvery { recipeRepository.getAllRecipes() } returns flowOf(listOf(cookableRecipe, uncookableRecipe))
        coEvery { productRepository.getAllProducts() } returns flowOf(listOf(product1, product2))

        // When & Then
        useCase().test {
            val result = awaitItem()

            // Verificar que solo retorna la receta cocinables
            assertEquals(1, result.size)
            assertEquals("Pasta con tomate", result[0].name)

            awaitComplete()
        }
    }

    @Test
    fun `invoke returns empty list when no recipes are cookable`() = runTest {
        // Given
        val product = Product(
            id = "1",
            name = "Leche",
            category = Category.DAIRY,
            quantity = 1f,
            unit = Unit.LITERS,
            lowStockThreshold = 0.5f,
            location = null,
            brand = null,
            updatedAt = System.currentTimeMillis()
        )

        val recipe = Recipe(
            id = "1",
            name = "Pizza",
            imageUrl = null,
            prepTimeMinutes = 30,
            servings = 4,
            difficulty = Difficulty.MEDIUM,
            steps = listOf("Hacer masa"),
            ingredients = listOf(
                Ingredient("i1", "1", "Harina", 300f, Unit.GRAMS, false),
                Ingredient("i2", "1", "Queso", 100f, Unit.GRAMS, false)
            ),
            createdBy = "user1",
            isPublic = false,
            createdAt = System.currentTimeMillis()
        )

        coEvery { recipeRepository.getAllRecipes() } returns flowOf(listOf(recipe))
        coEvery { productRepository.getAllProducts() } returns flowOf(listOf(product))

        // When & Then
        useCase().test {
            val result = awaitItem()

            assertTrue(result.isEmpty())

            awaitComplete()
        }
    }

    @Test
    fun `invoke handles unit conversion correctly`() = runTest {
        // Given - Tenemos 1kg de pasta, receta pide 200g
        val product = Product(
            id = "1",
            name = "Pasta",
            category = Category.GRAINS,
            quantity = 1f,
            unit = Unit.KILOGRAMS, // ← 1 kg
            lowStockThreshold = 0.2f,
            location = null,
            brand = null,
            updatedAt = System.currentTimeMillis()
        )

        val recipe = Recipe(
            id = "1",
            name = "Pasta simple",
            imageUrl = null,
            prepTimeMinutes = 15,
            servings = 2,
            difficulty = Difficulty.EASY,
            steps = listOf("Cocinar"),
            ingredients = listOf(
                Ingredient("i1", "1", "Pasta", 200f, Unit.GRAMS, false) // ← 200g
            ),
            createdBy = "user1",
            isPublic = false,
            createdAt = System.currentTimeMillis()
        )

        coEvery { recipeRepository.getAllRecipes() } returns flowOf(listOf(recipe))
        coEvery { productRepository.getAllProducts() } returns flowOf(listOf(product))

        // When & Then
        useCase().test {
            val result = awaitItem()

            // 1kg >= 200g → debe ser cocinables
            assertEquals(1, result.size)
            assertEquals("Pasta simple", result[0].name)

            awaitComplete()
        }
    }

    @Test
    fun `invoke filters out recipes with insufficient quantity`() = runTest {
        // Given
        val product = Product(
            id = "1",
            name = "Arroz",
            category = Category.GRAINS,
            quantity = 100f, // Solo 100g
            unit = Unit.GRAMS,
            lowStockThreshold = 50f,
            location = null,
            brand = null,
            updatedAt = System.currentTimeMillis()
        )

        val recipe = Recipe(
            id = "1",
            name = "Arroz con pollo",
            imageUrl = null,
            prepTimeMinutes = 40,
            servings = 4,
            difficulty = Difficulty.MEDIUM,
            steps = listOf("Cocinar arroz", "Añadir pollo"),
            ingredients = listOf(
                Ingredient("i1", "1", "Arroz", 300f, Unit.GRAMS, false) // Necesita 300g
            ),
            createdBy = "user1",
            isPublic = false,
            createdAt = System.currentTimeMillis()
        )

        coEvery { recipeRepository.getAllRecipes() } returns flowOf(listOf(recipe))
        coEvery { productRepository.getAllProducts() } returns flowOf(listOf(product))

        // When & Then
        useCase().test {
            val result = awaitItem()

            // 100g < 300g → no debe ser cocinables
            assertTrue(result.isEmpty())

            awaitComplete()
        }
    }

    @Test
    fun `invoke returns multiple cookable recipes`() = runTest {
        // Given
        val products = listOf(
            Product("1", "Huevos", Category.PROTEINS, 12f, Unit.UNITS, 6f, null, null, System.currentTimeMillis()),
            Product("2", "Pan", Category.GRAINS, 10f, Unit.UNITS, 2f, null, null, System.currentTimeMillis())
        )

        val recipe1 = Recipe(
            id = "1",
            name = "Huevos revueltos",
            imageUrl = null,
            prepTimeMinutes = 10,
            servings = 2,
            difficulty = Difficulty.EASY,
            steps = listOf("Batir huevos", "Cocinar"),
            ingredients = listOf(
                Ingredient("i1", "1", "Huevos", 3f, Unit.UNITS, false)
            ),
            createdBy = "user1",
            isPublic = false,
            createdAt = System.currentTimeMillis()
        )

        val recipe2 = Recipe(
            id = "2",
            name = "Tostadas",
            imageUrl = null,
            prepTimeMinutes = 5,
            servings = 1,
            difficulty = Difficulty.EASY,
            steps = listOf("Tostar pan"),
            ingredients = listOf(
                Ingredient("i2", "2", "Pan", 2f, Unit.UNITS, false)
            ),
            createdBy = "user1",
            isPublic = false,
            createdAt = System.currentTimeMillis()
        )

        coEvery { recipeRepository.getAllRecipes() } returns flowOf(listOf(recipe1, recipe2))
        coEvery { productRepository.getAllProducts() } returns flowOf(products)

        // When & Then
        useCase().test {
            val result = awaitItem()

            assertEquals(2, result.size)
            assertTrue(result.any { it.name == "Huevos revueltos" })
            assertTrue(result.any { it.name == "Tostadas" })

            awaitComplete()
        }
    }
}