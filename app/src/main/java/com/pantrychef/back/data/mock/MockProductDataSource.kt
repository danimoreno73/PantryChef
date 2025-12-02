package com.pantrychef.back.data.mock

import android.content.Context
import com.pantrychef.back.model.Product
import com.pantrychef.back.model.enums.Category
import com.pantrychef.back.model.enums.Unit

class MockProductDataSource(private val context: Context) {

    fun loadMockProducts(): List<Product> {
        val now = System.currentTimeMillis()

        return listOf(
            // --- INGREDIENTES PARA LA ENSALADA (COOKABLE) ---
            Product("1", "Tomate", Category.VEGETABLES, 6f, Unit.UNITS, 2f, "Nevera", null, now),
            Product("2", "Mozzarella fresca", Category.DAIRY, 2f, Unit.UNITS, 1f, "Nevera", "Galbani", now),
            Product("3", "Albahaca", Category.VEGETABLES, 50f, Unit.GRAMS, 10f, "Nevera", null, now),
            Product("4", "Aceite de oliva", Category.CONDIMENTS, 50f, Unit.TABLESPOONS, 0.2f, "Despensa", "Carbonell", now), // Usaremos este para todo

            // --- INGREDIENTES PARA EL ARROZ A LA CUBANA (COOKABLE) ---
            // Nota: Aquí pruebo la conversión. Tengo 1KG, la receta pide 100g. Debería funcionar.
            Product("5", "Arroz redondo", Category.GRAINS, 1f, Unit.KILOGRAMS, 0.5f, "Despensa", "SOS", now),
            Product("6", "Huevos", Category.PROTEINS, 12f, Unit.UNITS, 4f, "Nevera", "L", now),
            Product("7", "Tomate Frito", Category.CONDIMENTS, 300f, Unit.GRAMS, 100f, "Despensa", "Orlando", now),
            Product("8", "Plátano", Category.FRUITS, 5f, Unit.UNITS, 2f, "Frutero", "Canarias", now),

            // --- INGREDIENTES PARA BURGER (ALMOST - Falta Lechuga) ---
            Product("9", "Pan de Burger", Category.GRAINS, 4f, Unit.UNITS, 2f, "Despensa", "Bimbo", now),
            Product("10", "Carne Picada", Category.PROTEINS, 500f, Unit.GRAMS, 200f, "Nevera", "Carnicería", now),
            Product("11", "Queso Cheddar", Category.DAIRY, 10f, Unit.UNITS, 2f, "Nevera", "Hochland", now),
            // NO TENEMOS LECHUGA

            // --- INGREDIENTES PARA BATIDO (ALMOST - Falta Hielo) ---
            Product("12", "Leche entera", Category.DAIRY, 6f, Unit.LITERS, 2f, "Despensa", "Pascual", now),
            // Plátano ya lo tenemos (id 8)
            Product("13", "Fresas", Category.FRUITS, 20f, Unit.UNITS, 5f, "Nevera", null, now),
            Product("14", "Proteína en polvo", Category.OTHERS, 1f, Unit.KILOGRAMS, 0.2f, "Gimnasio", "Whey", now),
            // NO TENEMOS HIELO

            // --- INGREDIENTES PARA SUSHI (NADA - Tenemos solo arroz y vinagre) ---
            Product("15", "Arroz Sushi", Category.GRAINS, 1f, Unit.KILOGRAMS, 0.5f, "Despensa", null, now),
            Product("16", "Vinagre de arroz", Category.CONDIMENTS, 250f, Unit.MILLILITERS, 50f, "Despensa", null, now),
            // NO TENEMOS NORI
            // NO TENEMOS SALMÓN

            // --- INGREDIENTES PAELLA ---
            // Arroz redondo ya tenemos (id 5)
            // NO TENEMOS CALDO, NI GAMBAS, NI MEJILLONES
        )
    }
}