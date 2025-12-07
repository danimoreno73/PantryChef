package com.pantrychef.back.utils

import com.pantrychef.back.model.enums.Unit

object UnitsConverter {

    /**
     * Convierte una cantidad de una unidad a otra
     * @param quantity Cantidad a convertir
     * @param from Unidad origen
     * @param to Unidad destino
     * @return Cantidad convertida, o null si la conversión no es posible
     */
    fun convert(quantity: Float, from: Unit, to: Unit): Float? {
        if (from == to) return quantity


        return when {

            from == Unit.KILOGRAMS && to == Unit.GRAMS -> quantity * 1000f
            from == Unit.GRAMS && to == Unit.KILOGRAMS -> quantity / 1000f


            from == Unit.LITERS && to == Unit.MILLILITERS -> quantity * 1000f
            from == Unit.MILLILITERS && to == Unit.LITERS -> quantity / 1000f


            // 1 taza = 16 cucharadas (estándar)
            from == Unit.TABLESPOONS && to == Unit.CUPS -> quantity / 16f
            from == Unit.CUPS && to == Unit.TABLESPOONS -> quantity * 16f


            // 1 docena = 12 unidades
            from == Unit.DOZEN && to == Unit.UNITS -> quantity * 12f
            from == Unit.UNITS && to == Unit.DOZEN -> quantity / 12f


            // 1 taza líquida ≈ 240 ml
            from == Unit.CUPS && to == Unit.MILLILITERS -> quantity * 240f
            from == Unit.MILLILITERS && to == Unit.CUPS -> quantity / 240f
            from == Unit.CUPS && to == Unit.LITERS -> quantity * 0.24f
            from == Unit.LITERS && to == Unit.CUPS -> quantity / 0.24f


            else -> null
        }
    }

    /**
     * Compara si hay suficiente cantidad considerando conversión de unidades
     * @param productQuantity Cantidad disponible del producto
     * @param productUnit Unidad del producto
     * @param requiredQuantity Cantidad requerida
     * @param requiredUnit Unidad requerida
     * @return true si hay suficiente cantidad (después de conversión si es posible)
     */
    fun hasSufficientQuantity(
        productQuantity: Float,
        productUnit: Unit,
        requiredQuantity: Float,
        requiredUnit: Unit
    ): Boolean {

        if (productUnit == requiredUnit) {
            return productQuantity >= requiredQuantity
        }

        val convertedRequired = convert(requiredQuantity, requiredUnit, productUnit)

        if (convertedRequired != null) {
            return productQuantity >= convertedRequired
        }


        if (areSameType(productUnit, requiredUnit)) {
            val (normProduct, baseUnit) = normalizeToBaseUnit(productQuantity, productUnit)
            val (normRequired, _) = normalizeToBaseUnit(requiredQuantity, requiredUnit)
            return normProduct >= normRequired
        }

        // Unidades incompatibles (ej: gramos vs unidades)
        // Asumimos de momento que SÍ hay suficiente si no podemos comparar
        // Esto evita falsos negativos cuando el usuario usa diferentes sistemas

        return true
    }

    /**
     * Normaliza una cantidad a una unidad base para comparaciones
     * - Peso → gramos
     * - Volumen → mililitros
     * - Conteo → unidades
     * - Cocina → cucharadas
     * @return Par de (cantidad normalizada, unidad base)
     */
    fun normalizeToBaseUnit(quantity: Float, unit: Unit): Pair<Float, Unit> {
        return when (unit) {
            Unit.KILOGRAMS -> Pair(quantity * 1000f, Unit.GRAMS)
            Unit.GRAMS -> Pair(quantity, Unit.GRAMS)

            Unit.LITERS -> Pair(quantity * 1000f, Unit.MILLILITERS)
            Unit.MILLILITERS -> Pair(quantity, Unit.MILLILITERS)

            Unit.DOZEN -> Pair(quantity * 12f, Unit.UNITS)
            Unit.UNITS -> Pair(quantity, Unit.UNITS)
            Unit.PACKAGES -> Pair(quantity, Unit.PACKAGES)

            Unit.CUPS -> Pair(quantity * 16f, Unit.TABLESPOONS)
            Unit.TABLESPOONS -> Pair(quantity, Unit.TABLESPOONS)
        }
    }

    /**
     * Verifica si dos unidades son del mismo tipo (categoría)
     * @return true si ambas unidades son del mismo tipo
     */
    fun areSameType(unit1: Unit, unit2: Unit): Boolean {
        val weightUnits = setOf(Unit.GRAMS, Unit.KILOGRAMS)
        val volumeUnits = setOf(Unit.LITERS, Unit.MILLILITERS)
        val countUnits = setOf(Unit.UNITS, Unit.PACKAGES, Unit.DOZEN)
        val cookingUnits = setOf(Unit.TABLESPOONS, Unit.CUPS)

        return when (unit1) {
            in weightUnits if unit2 in weightUnits -> true
            in volumeUnits if unit2 in volumeUnits -> true
            in countUnits if unit2 in countUnits -> true
            in cookingUnits if unit2 in cookingUnits -> true
            else -> false
        }
    }

    /**
     * Obtiene el tipo de categoría de una unidad
     * Útil para mensajes de error o validaciones
     */
    fun getUnitType(unit: Unit): String {
        return when (unit) {
            Unit.GRAMS, Unit.KILOGRAMS -> "Weight"
            Unit.LITERS, Unit.MILLILITERS -> "Volume"
            Unit.UNITS, Unit.PACKAGES, Unit.DOZEN -> "Count"
            Unit.TABLESPOONS, Unit.CUPS -> "Kitchen measurement"
        }
    }

    /**
     * Formatea una cantidad con su unidad para mostrar al usuario
     * Simplifica cuando es posible (ej: 1000g → 1kg)
     */
    fun formatQuantity(quantity: Float, unit: Unit): String {
        val (displayQuantity, displayUnit) = when {

            unit == Unit.GRAMS && quantity >= 1000f -> {
                Pair(quantity / 1000f, Unit.KILOGRAMS)
            }

            unit == Unit.MILLILITERS && quantity >= 1000f -> {
                Pair(quantity / 1000f, Unit.LITERS)
            }

            unit == Unit.UNITS && quantity >= 12f && quantity % 12f == 0f -> {
                Pair(quantity / 12f, Unit.DOZEN)
            }

            unit == Unit.TABLESPOONS && quantity >= 16f && quantity % 16f == 0f -> {
                Pair(quantity / 16f, Unit.CUPS)
            }
            else -> Pair(quantity, unit)
        }

        val formattedNumber = if (displayQuantity % 1f == 0f) {
            displayQuantity.toInt().toString()
        } else {
            String.format("%.1f", displayQuantity)
        }


        val unitName = when (displayUnit) {
            Unit.GRAMS -> if (displayQuantity == 1f) "gramo" else "gramos"
            Unit.KILOGRAMS -> if (displayQuantity == 1f) "kilogramo" else "kilogramos"
            Unit.LITERS -> if (displayQuantity == 1f) "litro" else "litros"
            Unit.MILLILITERS -> "ml"
            Unit.UNITS -> if (displayQuantity == 1f) "unidad" else "unidades"
            Unit.PACKAGES -> if (displayQuantity == 1f) "paquete" else "paquetes"
            Unit.DOZEN -> if (displayQuantity == 1f) "docena" else "docenas"
            Unit.TABLESPOONS -> if (displayQuantity == 1f) "cucharada" else "cucharadas"
            Unit.CUPS -> if (displayQuantity == 1f) "taza" else "tazas"
        }

        return "$formattedNumber $unitName"
    }
}
