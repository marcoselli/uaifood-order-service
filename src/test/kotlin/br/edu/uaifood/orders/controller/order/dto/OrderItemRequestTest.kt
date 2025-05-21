package br.edu.uaifood.orders.controller.order.dto

import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals

class OrderItemRequestTest {
    @Test
    fun `should create request with correct data`() {
        // Given
        val productId = UUID.randomUUID()
        val quantity = 2
        val price = 10.0

        // When
        val request = OrderItemRequest(
            productId = productId,
            quantity = quantity,
            price = price
        )

        // Then
        assertEquals(productId, request.productId)
        assertEquals(quantity, request.quantity)
        assertEquals(price, request.price)
    }
} 