package com.pantrychef.back.utils

import com.pantrychef.back.model.enums.Unit
import org.junit.Assert.*
import org.junit.Test

class UnitsConverterTest {

    @Test
    fun `convert 1 kilogram to grams returns 1000`() {
        // Given
        val quantity = 1f
        val from = Unit.KILOGRAMS
        val to = Unit.GRAMS

        // When
        val result = UnitsConverter.convert(quantity, from, to)

        // Then
        assertEquals(1000f, result)
    }

    @Test
    fun `convert 500 grams to kilograms returns 0_5`() {
        // Given
        val quantity = 500f
        val from = Unit.GRAMS
        val to = Unit.KILOGRAMS

        // When
        val result = UnitsConverter.convert(quantity, from, to)

        // Then
        assertEquals(0.5f, result)
    }

    @Test
    fun `convert 1 liter to milliliters returns 1000`() {
        // Given
        val quantity = 1f
        val from = Unit.LITERS
        val to = Unit.MILLILITERS

        // When
        val result = UnitsConverter.convert(quantity, from, to)

        // Then
        assertEquals(1000f, result)
    }

    @Test
    fun `convert 250 milliliters to liters returns 0_25`() {
        // Given
        val quantity = 250f
        val from = Unit.MILLILITERS
        val to = Unit.LITERS

        // When
        val result = UnitsConverter.convert(quantity, from, to)

        // Then
        assertEquals(0.25f, result)
    }

    @Test
    fun `convert 1 dozen to units returns 12`() {
        // Given
        val quantity = 1f
        val from = Unit.DOZEN
        val to = Unit.UNITS

        // When
        val result = UnitsConverter.convert(quantity, from, to)

        // Then
        assertEquals(12f, result)
    }

    @Test
    fun `convert 24 units to dozen returns 2`() {
        // Given
        val quantity = 24f
        val from = Unit.UNITS
        val to = Unit.DOZEN

        // When
        val result = UnitsConverter.convert(quantity, from, to)

        // Then
        assertEquals(2f, result)
    }

    @Test
    fun `convert 1 cup to tablespoons returns 16`() {
        // Given
        val quantity = 1f
        val from = Unit.CUPS
        val to = Unit.TABLESPOONS

        // When
        val result = UnitsConverter.convert(quantity, from, to)

        // Then
        assertEquals(16f, result)
    }

    @Test
    fun `convert between incompatible units returns null`() {
        // Given
        val quantity = 100f
        val from = Unit.GRAMS
        val to = Unit.UNITS

        // When
        val result = UnitsConverter.convert(quantity, from, to)

        // Then
        assertNull(result)
    }

    @Test
    fun `convert same unit returns same quantity`() {
        // Given
        val quantity = 250f
        val unit = Unit.GRAMS

        // When
        val result = UnitsConverter.convert(quantity, unit, unit)

        // Then
        assertEquals(250f, result)
    }

    @Test
    fun `hasSufficientQuantity with 1kg available and 200g needed returns true`() {
        // Given
        val productQuantity = 1f
        val productUnit = Unit.KILOGRAMS
        val requiredQuantity = 200f
        val requiredUnit = Unit.GRAMS

        // When
        val result = UnitsConverter.hasSufficientQuantity(
            productQuantity = productQuantity,
            productUnit = productUnit,
            requiredQuantity = requiredQuantity,
            requiredUnit = requiredUnit
        )

        // Then
        assertTrue(result)
    }

    @Test
    fun `hasSufficientQuantity with 100g available and 1kg needed returns false`() {
        // Given
        val productQuantity = 100f
        val productUnit = Unit.GRAMS
        val requiredQuantity = 1f
        val requiredUnit = Unit.KILOGRAMS

        // When
        val result = UnitsConverter.hasSufficientQuantity(
            productQuantity = productQuantity,
            productUnit = productUnit,
            requiredQuantity = requiredQuantity,
            requiredUnit = requiredUnit
        )

        // Then
        assertFalse(result)
    }

    @Test
    fun `hasSufficientQuantity with same unit compares directly`() {
        // Given
        val productQuantity = 500f
        val requiredQuantity = 200f
        val unit = Unit.GRAMS

        // When
        val result = UnitsConverter.hasSufficientQuantity(
            productQuantity = productQuantity,
            productUnit = unit,
            requiredQuantity = requiredQuantity,
            requiredUnit = unit
        )

        // Then
        assertTrue(result)
    }

    @Test
    fun `hasSufficientQuantity with incompatible units returns true to avoid false negatives`() {
        // Given - Tengo 200g de cebolla, receta pide 1 unidad
        val productQuantity = 200f
        val productUnit = Unit.GRAMS
        val requiredQuantity = 1f
        val requiredUnit = Unit.UNITS

        // When
        val result = UnitsConverter.hasSufficientQuantity(
            productQuantity = productQuantity,
            productUnit = productUnit,
            requiredQuantity = requiredQuantity,
            requiredUnit = requiredUnit
        )

        // Then - Asume que SÍ hay para evitar falsos negativos
        assertTrue(result)
    }

    @Test
    fun `normalizeToBaseUnit converts kilograms to grams`() {
        // Given
        val quantity = 2f
        val unit = Unit.KILOGRAMS

        // When
        val (normalizedQuantity, normalizedUnit) = UnitsConverter.normalizeToBaseUnit(quantity, unit)

        // Then
        assertEquals(2000f, normalizedQuantity)
        assertEquals(Unit.GRAMS, normalizedUnit)
    }

    @Test
    fun `normalizeToBaseUnit converts liters to milliliters`() {
        // Given
        val quantity = 1.5f
        val unit = Unit.LITERS

        // When
        val (normalizedQuantity, normalizedUnit) = UnitsConverter.normalizeToBaseUnit(quantity, unit)

        // Then
        assertEquals(1500f, normalizedQuantity)
        assertEquals(Unit.MILLILITERS, normalizedUnit)
    }

    @Test
    fun `normalizeToBaseUnit converts dozen to units`() {
        // Given
        val quantity = 2f
        val unit = Unit.DOZEN

        // When
        val (normalizedQuantity, normalizedUnit) = UnitsConverter.normalizeToBaseUnit(quantity, unit)

        // Then
        assertEquals(24f, normalizedQuantity)
        assertEquals(Unit.UNITS, normalizedUnit)
    }

    @Test
    fun `normalizeToBaseUnit converts cups to tablespoons`() {
        // Given
        val quantity = 2f
        val unit = Unit.CUPS

        // When
        val (normalizedQuantity, normalizedUnit) = UnitsConverter.normalizeToBaseUnit(quantity, unit)

        // Then
        assertEquals(32f, normalizedQuantity)
        assertEquals(Unit.TABLESPOONS, normalizedUnit)
    }

    @Test
    fun `normalizeToBaseUnit keeps grams as base unit`() {
        // Given
        val quantity = 500f
        val unit = Unit.GRAMS

        // When
        val (normalizedQuantity, normalizedUnit) = UnitsConverter.normalizeToBaseUnit(quantity, unit)

        // Then
        assertEquals(500f, normalizedQuantity)
        assertEquals(Unit.GRAMS, normalizedUnit)
    }

    @Test
    fun `areSameType returns true for weight units`() {
        // When
        val result = UnitsConverter.areSameType(Unit.GRAMS, Unit.KILOGRAMS)

        // Then
        assertTrue(result)
    }

    @Test
    fun `areSameType returns true for volume units`() {
        // When
        val result = UnitsConverter.areSameType(Unit.LITERS, Unit.MILLILITERS)

        // Then
        assertTrue(result)
    }

    @Test
    fun `areSameType returns true for count units`() {
        // When
        val result = UnitsConverter.areSameType(Unit.UNITS, Unit.DOZEN)

        // Then
        assertTrue(result)
    }

    @Test
    fun `areSameType returns true for cooking units`() {
        // When
        val result = UnitsConverter.areSameType(Unit.TABLESPOONS, Unit.CUPS)

        // Then
        assertTrue(result)
    }

    @Test
    fun `areSameType returns false for different types`() {
        // When
        val result = UnitsConverter.areSameType(Unit.GRAMS, Unit.LITERS)

        // Then
        assertFalse(result)
    }

    @Test
    fun `areSameType returns false for weight and count`() {
        // When
        val result = UnitsConverter.areSameType(Unit.GRAMS, Unit.UNITS)

        // Then
        assertFalse(result)
    }

    @Test
    fun `formatQuantity simplifies 1000 grams to 1 kilogram`() {
        // Given
        val quantity = 1000f
        val unit = Unit.GRAMS

        // When
        val result = UnitsConverter.formatQuantity(quantity, unit)

        // Then
        assertEquals("1 kilogram", result)
    }

    @Test
    fun `formatQuantity keeps 500 grams as grams`() {
        // Given
        val quantity = 500f
        val unit = Unit.GRAMS

        // When
        val result = UnitsConverter.formatQuantity(quantity, unit)

        // Then
        assertEquals("500 grams", result)
    }

    @Test
    fun `formatQuantity simplifies 1000 milliliters to 1 liter`() {
        // Given
        val quantity = 1000f
        val unit = Unit.MILLILITERS

        // When
        val result = UnitsConverter.formatQuantity(quantity, unit)

        // Then
        assertEquals("1 liter", result)
    }

    @Test
    fun `formatQuantity handles singular unit names`() {
        // Given
        val quantity = 1f
        val unit = Unit.GRAMS

        // When
        val result = UnitsConverter.formatQuantity(quantity, unit)

        // Then
        assertEquals("1 gram", result)
    }
}