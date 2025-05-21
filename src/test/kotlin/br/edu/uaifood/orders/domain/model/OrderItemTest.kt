package br.edu.uaifood.orders.domain.model

import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals

class OrderItemTest {

    @Test
    fun `calculateSubtotal should return quantity times price`() {
        // Given
        val orderItem = OrderItem(
            productId = UUID.randomUUID(),
            quantity = 3,
            price = 10.0
        )

        // When
        val subtotal = orderItem.calculateSubtotal()

        // Then
        assertEquals(30.0, subtotal) // 3 * 10.0 = 30.0
    }

    @Test
    fun `calculateSubtotal should handle zero quantity`() {
        // Given
        val orderItem = OrderItem(
            productId = UUID.randomUUID(),
            quantity = 0,
            price = 10.0
        )

        // When
        val subtotal = orderItem.calculateSubtotal()

        // Then
        assertEquals(0.0, subtotal)
    }

    @Test
    fun `calculateSubtotal should handle zero price`() {
        // Given
        val orderItem = OrderItem(
            productId = UUID.randomUUID(),
            quantity = 3,
            price = 0.0
        )

        // When
        val subtotal = orderItem.calculateSubtotal()

        // Then
        assertEquals(0.0, subtotal)
    }
} 