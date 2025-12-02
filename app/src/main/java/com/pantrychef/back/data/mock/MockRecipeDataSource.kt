package com.pantrychef.back.data.mock

import android.content.Context
import com.pantrychef.back.model.Ingredient
import com.pantrychef.back.model.Recipe
import com.pantrychef.back.model.enums.Difficulty
import com.pantrychef.back.model.enums.Unit

class MockRecipeDataSource(private val context: Context) {

    fun loadMockRecipes(): List<Recipe> {
        val now = System.currentTimeMillis()

        return listOf(
            // ==========================================================
            // GRUPO 1: COMPLETAMENTE COCINABLES (100%)
            // ==========================================================

            // Receta 1: Ensalada Caprese (4 ingredientes, tenemos los 4)
            Recipe(
                id = "1",
                name = "Ensalada Caprese",
                imageUrl = "https://example.com/caprese.jpg",
                prepTimeMinutes = 10,
                servings = 2,
                difficulty = Difficulty.EASY,
                steps = listOf("Cortar tomate y mozzarella", "Aliñar"),
                ingredients = listOf(
                    Ingredient("i1", "1", "Tomate", 2f, Unit.UNITS, false),
                    Ingredient("i2", "1", "Mozzarella fresca", 1f, Unit.UNITS, false),
                    Ingredient("i3", "1", "Albahaca", 10f, Unit.GRAMS, false),
                    Ingredient("i4", "1", "Aceite de oliva", 2f, Unit.TABLESPOONS, false)
                ),
                createdBy = "system", isPublic = true, createdAt = now
            ),

            // Receta 2: Arroz a la Cubana (4 ingredientes, tenemos los 4)
            // PRUEBA DE FUEGO: La receta pide GRAMOS de arroz, pero tenemos KILOS en despensa.
            Recipe(
                id = "2",
                name = "Arroz a la Cubana",
                imageUrl = null,
                prepTimeMinutes = 20,
                servings = 1,
                difficulty = Difficulty.EASY,
                steps = listOf("Cocer arroz", "Freír huevo", "Emplatar con tomate y plátano"),
                ingredients = listOf(
                    Ingredient("i5", "2", "Arroz redondo", 100f, Unit.GRAMS, false), // Tenemos 1KG
                    Ingredient("i6", "2", "Huevos", 2f, Unit.UNITS, false),
                    Ingredient("i7", "2", "Tomate Frito", 100f, Unit.GRAMS, false),
                    Ingredient("i8", "2", "Plátano", 1f, Unit.UNITS, false)
                ),
                createdBy = "system", isPublic = true, createdAt = now
            ),

            // ==========================================================
            // GRUPO 2: CASI COCINABLES (70% - 99%)
            // ==========================================================

            // Receta 3: Burger Casera (4 ingredientes. Tenemos 3. Ratio: 75%)
            // FALTA: Lechuga
            Recipe(
                id = "3",
                name = "Burger Clásica",
                imageUrl = null,
                prepTimeMinutes = 15,
                servings = 1,
                difficulty = Difficulty.MEDIUM,
                steps = listOf("Cocinar carne", "Montar burger"),
                ingredients = listOf(
                    Ingredient("i9", "3", "Pan de Burger", 1f, Unit.UNITS, false),
                    Ingredient("i10", "3", "Carne Picada", 150f, Unit.GRAMS, false),
                    Ingredient("i11", "3", "Queso Cheddar", 1f, Unit.UNITS, false),
                    Ingredient("i12", "3", "Lechuga", 2f, Unit.UNITS, false) // <-- ESTE FALTA
                ),
                createdBy = "system", isPublic = true, createdAt = now
            ),

            // Receta 4: Batido de Proteínas (5 ingredientes. Tenemos 4. Ratio: 80%)
            // FALTA: Hielo
            Recipe(
                id = "4",
                name = "Batido Post-Entreno",
                imageUrl = null,
                prepTimeMinutes = 5,
                servings = 1,
                difficulty = Difficulty.EASY,
                steps = listOf("Mezclar todo en batidora"),
                ingredients = listOf(
                    Ingredient("i13", "4", "Leche entera", 250f, Unit.MILLILITERS, false),
                    Ingredient("i14", "4", "Plátano", 1f, Unit.UNITS, false),
                    Ingredient("i15", "4", "Fresas", 5f, Unit.UNITS, false),
                    Ingredient("i16", "4", "Proteína en polvo", 30f, Unit.GRAMS, false),
                    Ingredient("i17", "4", "Hielo", 3f, Unit.UNITS, false) // <-- ESTE FALTA
                ),
                createdBy = "system", isPublic = true, createdAt = now
            ),

            // ==========================================================
            // GRUPO 3: INSUFICIENTE / NADA (< 70%)
            // ==========================================================

            // Receta 5: Sushi (4 ingredientes. Tenemos 2. Ratio: 50%)
            // FALTAN: Alga Nori y Salmón.
            Recipe(
                id = "5",
                name = "Maki Sushi",
                imageUrl = null,
                prepTimeMinutes = 60,
                servings = 2,
                difficulty = Difficulty.HARD,
                steps = listOf("Preparar arroz", "Enrollar"),
                ingredients = listOf(
                    Ingredient("i18", "5", "Arroz Sushi", 200f, Unit.GRAMS, false), // Tenemos
                    Ingredient("i19", "5", "Vinagre de arroz", 20f, Unit.MILLILITERS, false), // Tenemos
                    Ingredient("i20", "5", "Alga Nori", 2f, Unit.UNITS, false), // FALTA
                    Ingredient("i21", "5", "Salmón fresco", 100f, Unit.GRAMS, false) // FALTA
                ),
                createdBy = "system", isPublic = true, createdAt = now
            ),

            // Receta 6: Paella (4 ingredientes. Tenemos 1. Ratio: 25%)
            // SOLO TENEMOS: Arroz. FALTA: Todo lo demás.
            Recipe(
                id = "6",
                name = "Paella de Marisco",
                imageUrl = null,
                prepTimeMinutes = 45,
                servings = 4,
                difficulty = Difficulty.HARD,
                steps = listOf("Sofreír", "Cocer arroz"),
                ingredients = listOf(
                    Ingredient("i22", "6", "Arroz redondo", 400f, Unit.GRAMS, false), // Tenemos (el mismo de la cubana)
                    Ingredient("i23", "6", "Caldo de pescado", 1f, Unit.LITERS, false), // FALTA
                    Ingredient("i24", "6", "Gambas", 200f, Unit.GRAMS, false), // FALTA
                    Ingredient("i25", "6", "Mejillones", 200f, Unit.GRAMS, false) // FALTA
                ),
                createdBy = "system", isPublic = true, createdAt = now
            )
        )
    }
}