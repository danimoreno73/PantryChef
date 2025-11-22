package com.pantrychef.back.data.mock

import android.content.Context
import com.pantrychef.back.model.Ingredient
import com.pantrychef.back.model.Recipe
import com.pantrychef.back.model.enums.Difficulty
import com.pantrychef.back.model.enums.Unit

class MockRecipeDataSource(private val context: Context) {

    fun loadMockRecipes(): List<Recipe> {
        return listOf(
            Recipe(
                id = "1",
                name = "Pasta con verduras",
                imageUrl = null,
                prepTimeMinutes = 25,
                servings = 2,
                difficulty = Difficulty.EASY,
                steps = listOf(
                    "Hervir la pasta en agua con sal hasta al dente. Reserva 1/4 taza del agua.",
                    "Saltear verduras (calabacín y pimiento) en aceite de oliva 4-5 min, sal y pimienta.",
                    "Mezclar pasta escurrida con verduras. Ajustar condimentos.",
                    "Servir y terminar con aceite de oliva o queso si tienes."
                ),
                ingredients = listOf(
                    Ingredient(
                        id = "i1",
                        recipeId = "1",
                        productName = "Pasta corta",
                        quantity = 180f,
                        unit = Unit.GRAMS,
                        isOptional = false
                    ),
                    Ingredient(
                        id = "i2",
                        recipeId = "1",
                        productName = "Calabacín",
                        quantity = 1f,
                        unit = Unit.UNITS,
                        isOptional = false
                    ),
                    Ingredient(
                        id = "i3",
                        recipeId = "1",
                        productName = "Pimiento",
                        quantity = 1f,
                        unit = Unit.UNITS,
                        isOptional = false
                    ),
                    Ingredient(
                        id = "i4",
                        recipeId = "1",
                        productName = "Aceite de oliva",
                        quantity = 1f,
                        unit = Unit.TABLESPOONS,
                        isOptional = false
                    )
                ),
                createdBy = "mock-user",
                isPublic = false,
                createdAt = System.currentTimeMillis()
            ),
            Recipe(
                id = "2",
                name = "Tacos de pollo",
                imageUrl = null,
                prepTimeMinutes = 30,
                servings = 3,
                difficulty = Difficulty.MEDIUM,
                steps = listOf(
                    "Preparar relleno: saltear cebolla y agregar pollo; condimentar al gusto.",
                    "Calentar tortillas en plancha o sartén 30-60 s por lado.",
                    "Montar tacos: rellenar con pollo; terminar con jugo de lima."
                ),
                ingredients = listOf(
                    Ingredient(
                        id = "i5",
                        recipeId = "2",
                        productName = "Pollo desmenuzado",
                        quantity = 300f,
                        unit = Unit.GRAMS,
                        isOptional = false
                    ),
                    Ingredient(
                        id = "i6",
                        recipeId = "2",
                        productName = "Cebolla",
                        quantity = 1f,
                        unit = Unit.UNITS,
                        isOptional = false
                    ),
                    Ingredient(
                        id = "i7",
                        recipeId = "2",
                        productName = "Tortillas", // FALTA - para probar casi cocinables
                        quantity = 12f,
                        unit = Unit.UNITS,
                        isOptional = false
                    ),
                    Ingredient(
                        id = "i8",
                        recipeId = "2",
                        productName = "Lima",
                        quantity = 1f,
                        unit = Unit.UNITS,
                        isOptional = false
                    )
                ),
                createdBy = "mock-user",
                isPublic = false,
                createdAt = System.currentTimeMillis()
            )
        )
    }
}