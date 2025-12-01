package com.pantrychef.back.data.mock

import android.content.Context
import com.pantrychef.back.model.Product
import com.pantrychef.back.model.enums.Category
import com.pantrychef.back.model.enums.Unit

class MockProductDataSource(private val context: Context) {

    fun loadMockProducts(): List<Product> {
        /*
        val jsonString = context.assets.open("./seed/products.json")
            .bufferedReader()
            .use { it.readText() }
        */
        // Parsear JSON manualmente o con Gson/Moshi
        return parseProductsFromJson("jsonString")
    }

    private fun parseProductsFromJson(json: String): List<Product> {
        // TODO: Implementar parseo real o usar librería (Moshi, Kotlinx Serialization, etc.)
        // Por ahora: lista hardcoded para mocks
        val now = System.currentTimeMillis()

        return listOf(
            Product(
                id = "1",
                name = "Leche entera",
                category = Category.DAIRY,
                quantity = 2.0f,
                unit = Unit.LITERS,
                lowStockThreshold = 1.0f,
                location = "Refrigerador",
                brand = "Pascual",
                updatedAt = now
            ),
            Product(
                id = "2",
                name = "Yogur natural",
                category = Category.DAIRY,
                quantity = 6.0f,
                unit = Unit.UNITS,
                lowStockThreshold = 4.0f,
                location = "Refrigerador",
                brand = "Danone",
                updatedAt = now
            ),
            Product(
                id = "3",
                name = "Huevos",
                category = Category.PROTEINS,
                quantity = 3.0f, // Bajo stock
                unit = Unit.UNITS,
                lowStockThreshold = 6.0f,
                location = "Refrigerador",
                brand = null,
                updatedAt = now
            ),
            Product(
                id = "4",
                name = "Pechuga de pollo",
                category = Category.PROTEINS,
                quantity = 0.6f,
                unit = Unit.KILOGRAMS,
                lowStockThreshold = 0.5f,
                location = "Refrigerador",
                brand = null,
                updatedAt = now
            ),
            Product(
                id = "5",
                name = "Lentejas",
                category = Category.PROTEINS,
                quantity = 0.8f,
                unit = Unit.KILOGRAMS,
                lowStockThreshold = 0.5f,
                location = "Despensa",
                brand = "Legumbre",
                updatedAt = now
            ),
            Product(
                id = "6",
                name = "Arroz",
                category = Category.GRAINS,
                quantity = 1.5f,
                unit = Unit.KILOGRAMS,
                lowStockThreshold = 1.0f,
                location = "Despensa",
                brand = "SOS",
                updatedAt = now
            ),
            Product(
                id = "7",
                name = "Pasta",
                category = Category.GRAINS,
                quantity = 0.3f, // Casi sin stock
                unit = Unit.KILOGRAMS,
                lowStockThreshold = 0.5f,
                location = "Despensa",
                brand = "Barilla",
                updatedAt = now
            ),
            Product(
                id = "8",
                name = "Pan de molde",
                category = Category.GRAINS,
                quantity = 1.0f,
                unit = Unit.PACKAGES,
                lowStockThreshold = 1.0f,
                location = "Despensa",
                brand = "Bimbo",
                updatedAt = now
            ),
            Product(
                id = "9",
                name = "Tomates",
                category = Category.VEGETABLES,
                quantity = 4.0f,
                unit = Unit.UNITS,
                lowStockThreshold = 3.0f,
                location = "Frutero",
                brand = null,
                updatedAt = now
            ),
            Product(
                id = "10",
                name = "Cebollas",
                category = Category.VEGETABLES,
                quantity = 2.0f,
                unit = Unit.UNITS,
                lowStockThreshold = 2.0f,
                location = "Despensa",
                brand = null,
                updatedAt = now
            ),
            Product(
                id = "11",
                name = "Manzanas",
                category = Category.FRUITS,
                quantity = 1.0f,
                unit = Unit.KILOGRAMS,
                lowStockThreshold = 0.5f,
                location = "Frutero",
                brand = null,
                updatedAt = now
            ),
            Product(
                id = "12",
                name = "Plátanos",
                category = Category.FRUITS,
                quantity = 5.0f,
                unit = Unit.UNITS,
                lowStockThreshold = 3.0f,
                location = "Frutero",
                brand = null,
                updatedAt = now
            ),
            Product(
                id = "13",
                name = "Aceite de oliva virgen extra",
                category = Category.CONDIMENTS,
                quantity = 0.4f, // Bajo stock
                unit = Unit.LITERS,
                lowStockThreshold = 0.5f,
                location = "Despensa",
                brand = "Carbonell",
                updatedAt = now
            ),
            Product(
                id = "14",
                name = "Sal fina",
                category = Category.CONDIMENTS,
                quantity = 0.2f,
                unit = Unit.KILOGRAMS,
                lowStockThreshold = 0.1f,
                location = "Despensa",
                brand = null,
                updatedAt = now
            ),
            Product(
                id = "15",
                name = "Pimienta negra molida",
                category = Category.CONDIMENTS,
                quantity = 1.0f,
                unit = Unit.UNITS,
                lowStockThreshold = 1.0f,
                location = "Especiero",
                brand = null,
                updatedAt = now
            ),
            Product(
                id = "16",
                name = "Ketchup",
                category = Category.CONDIMENTS,
                quantity = 0.3f,
                unit = Unit.LITERS,
                lowStockThreshold = 0.2f,
                location = "Refrigerador",
                brand = "Heinz",
                updatedAt = now
            ),
            Product(
                id = "17",
                name = "Galletas",
                category = Category.OTHERS,
                quantity = 2.0f,
                unit = Unit.PACKAGES,
                lowStockThreshold = 1.0f,
                location = "Despensa",
                brand = "María",
                updatedAt = now
            ),
            Product(
                id = "18",
                name = "Café molido",
                category = Category.OTHERS,
                quantity = 0.25f,
                unit = Unit.KILOGRAMS,
                lowStockThreshold = 0.2f,
                location = "Despensa",
                brand = "Marcilla",
                updatedAt = now
            ),
            Product(
                id = "19",
                name = "Chocolate negro",
                category = Category.OTHERS,
                quantity = 3.0f,
                unit = Unit.UNITS,
                lowStockThreshold = 1.0f,
                location = "Despensa",
                brand = "Lindt",
                updatedAt = now
            ),
            Product(
                id = "20",
                name = "Letus",
                category = Category.OTHERS,
                quantity = 0.8f,
                unit = Unit.LITERS,
                lowStockThreshold = 0.5f,
                location = "Despensa",
                brand = "Calvo",
                updatedAt = now
            )
        )
    }

}


