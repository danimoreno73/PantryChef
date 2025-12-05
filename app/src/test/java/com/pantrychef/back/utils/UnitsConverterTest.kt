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
    fun `areSameType returns true for weight units`() {
        // When
        val result = UnitsConverter.areSameType(Unit.GRAMS, Unit.KILOGRAMS)

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
    fun `formatQuantity simplifies 1000 grams to 1 kilogram`() {
        // Given
        val quantity = 1000f
        val unit = Unit.GRAMS

        // When
        val result = UnitsConverter.formatQuantity(quantity, unit)

        // Then
        assertEquals("1 kilogram", result)
    }
}