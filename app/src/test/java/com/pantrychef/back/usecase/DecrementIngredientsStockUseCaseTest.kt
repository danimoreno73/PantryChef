package com.pantrychef.back.usecase

import com.pantrychef.back.model.Alert
import com.pantrychef.back.model.Ingredient
import com.pantrychef.back.model.Product
import com.pantrychef.back.model.Recipe
import com.pantrychef.back.model.enums.AlertType
import com.pantrychef.back.model.enums.Category
import com.pantrychef.back.model.enums.Difficulty
import com.pantrychef.back.model.enums.Severity
import com.pantrychef.back.model.enums.Unit
import com.pantrychef.back.repository.AlertRepository
import com.pantrychef.back.repository.ProductRepository
import com.pantrychef.back.repository.RecipeRepository
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class DecrementIngredientsStockUseCaseTest {

    private lateinit var recipeRepository: RecipeRepository
    private lateinit var productRepository: ProductRepository
    private lateinit var alertRepository: AlertRepository

    private lateinit var useCase: DecrementIngredientsStockUseCase

    @Before
    fun setup() {
        recipeRepository = mockk()
        productRepository = mockk()
        alertRepository = mockk()
        useCase = DecrementIngredientsStockUseCase(recipeRepository, productRepository, alertRepository)
    }

    @Test
    fun `invoke decrements product quantity correctly`() = runTest {
        // Given
        val recipe = Recipe(
            id = "1",
            name = "Pasta",
            imageUrl = null,
            prepTimeMinutes = 20,
            servings = 2,
            difficulty = Difficulty.EASY,
            steps = listOf("Cocinar"),
            ingredients = listOf(
                Ingredient("i1", "1", "Pasta", 200f, Unit.GRAMS, false)
            ),
            createdBy = "user1",
            isPublic = false,
            createdAt = System.currentTimeMillis()
        )

        val product = Product(
            id = "1",
            name = "Pasta",
            category = Category.GRAINS,
            quantity = 500f,
            unit = Unit.GRAMS,
            lowStockThreshold = 100f,
            location = null,
            brand = null,
            updatedAt = System.currentTimeMillis()
        )

        coEvery { recipeRepository.getRecipeById("1") } returns Result.success(recipe)
        coEvery { productRepository.getAllProducts() } returns flowOf(listOf(product))
        coEvery { productRepository.updateProduct(any()) } returns Result.success(Unit)
        coEvery { alertRepository.createAlert(any()) } returns Result.success(Unit)

        // When
        val result = useCase("1", servings = 2)

        // Then
        assertTrue(result.isSuccess)

        // Verificar que se llamó updateProduct con cantidad reducida
        val productSlot = slot<Product>()
        coVerify { productRepository.updateProduct(capture(productSlot)) }

        // 500g - 200g = 300g
        assertEquals(300f, productSlot.captured.quantity)
    }

    @Test
    fun `invoke creates alert when product goes below threshold`() = runTest {
        // Given
        val recipe = Recipe(
            id = "1",
            name = "Pasta",
            imageUrl = null,
            prepTimeMinutes = 20,
            servings = 2,
            difficulty = Difficulty.EASY,
            steps = listOf("Cocinar"),
            ingredients = listOf(
                Ingredient("i1", "1", "Pasta", 400f, Unit.GRAMS, false)
            ),
            createdBy = "user1",
            isPublic = false,
            createdAt = System.currentTimeMillis()
        )

        val product = Product(
            id = "1",
            name = "Pasta",
            category = Category.GRAINS,
            quantity = 500f,
            unit = Unit.GRAMS,
            lowStockThreshold = 200f, // ← Threshold = 200g
            location = null,
            brand = null,
            updatedAt = System.currentTimeMillis()
        )

        coEvery { recipeRepository.getRecipeById("1") } returns Result.success(recipe)
        coEvery { productRepository.getAllProducts() } returns flowOf(listOf(product))
        coEvery { productRepository.updateProduct(any()) } returns Result.success(Unit)
        coEvery { alertRepository.createAlert(any()) } returns Result.success(Unit)

        // When
        useCase("1", servings = 2)

        // Then
        // 500g - 400g = 100g (por debajo del threshold de 200g)
        val alertSlot = slot<Alert>()
        coVerify { alertRepository.createAlert(capture(alertSlot)) }

        assertEquals("Pasta", alertSlot.captured.productName)
        assertEquals(AlertType.LOW_STOCK, alertSlot.captured.alertType)
        assertFalse(alertSlot.captured.isResolved)
    }

    @Test
    fun `invoke creates URGENT alert when product reaches zero`() = runTest {
        // Given
        val recipe = Recipe(
            id = "1",
            name = "Arroz",
            imageUrl = null,
            prepTimeMinutes = 30,
            servings = 2,
            difficulty = Difficulty.EASY,
            steps = listOf("Cocinar arroz"),
            ingredients = listOf(
                Ingredient("i1", "1", "Arroz", 200f, Unit.GRAMS, false)
            ),
            createdBy = "user1",
            isPublic = false,
            createdAt = System.currentTimeMillis()
        )

        val product = Product(
            id = "1",
            name = "Arroz",
            category = Category.GRAINS,
            quantity = 200f, // Exactamente lo que necesita
            unit = Unit.GRAMS,
            lowStockThreshold = 100f,
            location = null,
            brand = null,
            updatedAt = System.currentTimeMillis()
        )

        coEvery { recipeRepository.getRecipeById("1") } returns Result.success(recipe)
        coEvery { productRepository.getAllProducts() } returns flowOf(listOf(product))
        coEvery { productRepository.updateProduct(any()) } returns Result.success(Unit)
        coEvery { alertRepository.createAlert(any()) } returns Result.success(Unit)

        // When
        useCase("1", servings = 2)

        // Then
        val alertSlot = slot<Alert>()
        coVerify { alertRepository.createAlert(capture(alertSlot)) }

        assertEquals(AlertType.OUT_OF_STOCK, alertSlot.captured.alertType)
        assertEquals(Severity.URGENT, alertSlot.captured.severity)
    }

    @Test
    fun `invoke adjusts quantity based on servings multiplier`() = runTest {
        // Given
        val recipe = Recipe(
            id = "1",
            name = "Pasta",
            imageUrl = null,
            prepTimeMinutes = 20,
            servings = 2, // ← Receta para 2 personas
            difficulty = Difficulty.EASY,
            steps = listOf("Cocinar"),
            ingredients = listOf(
                Ingredient("i1", "1", "Pasta", 200f, Unit.GRAMS, false) // 200g para 2 personas
            ),
            createdBy = "user1",
            isPublic = false,
            createdAt = System.currentTimeMillis()
        )

        val product = Product(
            id = "1",
            name = "Pasta",
            category = Category.GRAINS,
            quantity = 1000f,
            unit = Unit.GRAMS,
            lowStockThreshold = 100f,
            location = null,
            brand = null,
            updatedAt = System.currentTimeMillis()
        )

        coEvery { recipeRepository.getRecipeById("1") } returns Result.success(recipe)
        coEvery { productRepository.getAllProducts() } returns flowOf(listOf(product))
        coEvery { productRepository.updateProduct(any()) } returns Result.success(Unit)
        coEvery { alertRepository.createAlert(any()) } returns Result.success(Unit)

        // When - Cocinar para 4 personas (el doble)
        useCase("1", servings = 4)

        // Then
        val productSlot = slot<Product>()
        coVerify { productRepository.updateProduct(capture(productSlot)) }

        // servingFactor = 4 / 2 = 2
        // 200g * 2 = 400g a descontar
        // 1000g - 400g = 600g
        assertEquals(600f, productSlot.captured.quantity)
    }

    @Test
    fun `invoke handles unit conversion when decrementing`() = runTest {
        // Given - Producto en kg, ingrediente en g
        val recipe = Recipe(
            id = "1",
            name = "Pasta",
            imageUrl = null,
            prepTimeMinutes = 20,
            servings = 2,
            difficulty = Difficulty.EASY,
            steps = listOf("Cocinar"),
            ingredients = listOf(
                Ingredient("i1", "1", "Pasta", 300f, Unit.GRAMS, false) // 300g
            ),
            createdBy = "user1",
            isPublic = false,
            createdAt = System.currentTimeMillis()
        )

        val product = Product(
            id = "1",
            name = "Pasta",
            category = Category.GRAINS,
            quantity = 1f, // 1kg
            unit = Unit.KILOGRAMS,
            lowStockThreshold = 0.2f,
            location = null,
            brand = null,
            updatedAt = System.currentTimeMillis()
        )

        coEvery { recipeRepository.getRecipeById("1") } returns Result.success(recipe)
        coEvery { productRepository.getAllProducts() } returns flowOf(listOf(product))
        coEvery { productRepository.updateProduct(any()) } returns Result.success(Unit)
        coEvery { alertRepository.createAlert(any()) } returns Result.success(Unit)

        // When
        useCase("1", servings = 2)

        // Then
        val productSlot = slot<Product>()
        coVerify { productRepository.updateProduct(capture(productSlot)) }

        // 300g = 0.3kg
        // 1kg - 0.3kg = 0.7kg
        assertEquals(0.7f, productSlot.captured.quantity, 0.01f)
    }

    @Test
    fun `invoke prevents negative quantities`() = runTest {
        // Given - No hay suficiente producto
        val recipe = Recipe(
            id = "1",
            name = "Pasta",
            imageUrl = null,
            prepTimeMinutes = 20,
            servings = 2,
            difficulty = Difficulty.EASY,
            steps = listOf("Cocinar"),
            ingredients = listOf(
                Ingredient("i1", "1", "Pasta", 500f, Unit.GRAMS, false)
            ),
            createdBy = "user1",
            isPublic = false,
            createdAt = System.currentTimeMillis()
        )

        val product = Product(
            id = "1",
            name = "Pasta",
            category = Category.GRAINS,
            quantity = 200f, // Solo 200g disponibles
            unit = Unit.GRAMS,
            lowStockThreshold = 100f,
            location = null,
            brand = null,
            updatedAt = System.currentTimeMillis()
        )

        coEvery { recipeRepository.getRecipeById("1") } returns Result.success(recipe)
        coEvery { productRepository.getAllProducts() } returns flowOf(listOf(product))
        coEvery { productRepository.updateProduct(any()) } returns Result.success(Unit)
        coEvery { alertRepository.createAlert(any()) } returns Result.success(Unit)

        // When
        useCase("1", servings = 2)

        // Then
        val productSlot = slot<Product>()
        coVerify { productRepository.updateProduct(capture(productSlot)) }

        // 200g - 500g = -300g → pero se limita a 0
        assertEquals(0f, productSlot.captured.quantity)
    }

    @Test
    fun `invoke does not create alert when quantity stays above threshold`() = runTest {
        // Given
        val recipe = Recipe(
            id = "1",
            name = "Pasta",
            imageUrl = null,
            prepTimeMinutes = 20,
            servings = 2,
            difficulty = Difficulty.EASY,
            steps = listOf("Cocinar"),
            ingredients = listOf(
                Ingredient("i1", "1", "Pasta", 100f, Unit.GRAMS, false)
            ),
            createdBy = "user1",
            isPublic = false,
            createdAt = System.currentTimeMillis()
        )

        val product = Product(
            id = "1",
            name = "Pasta",
            category = Category.GRAINS,
            quantity = 1000f,
            unit = Unit.GRAMS,
            lowStockThreshold = 200f,
            location = null,
            brand = null,
            updatedAt = System.currentTimeMillis()
        )

        coEvery { recipeRepository.getRecipeById("1") } returns Result.success(recipe)
        coEvery { productRepository.getAllProducts() } returns flowOf(listOf(product))
        coEvery { productRepository.updateProduct(any()) } returns Result.success(Unit)

        // When
        useCase("1", servings = 2)

        // Then
        // 1000g - 100g = 900g (muy por encima del threshold de 200g)
        coVerify(exactly = 0) { alertRepository.createAlert(any()) }
    }
}