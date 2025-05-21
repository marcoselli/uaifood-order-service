package br.edu.uaifood.orders.controller.order.dto

import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals

class CreateOrderRequestTest {
    @Test
    fun `should create request with correct data`() {
        // Given
        val customerId = "customer123"
        val restaurantId = "restaurant456"
        val items = listOf(
            OrderItemRequest(
                productId = UUID.randomUUID(),
                quantity = 2,
                price = 10.0
            )
        )

        // When
        val request = CreateOrderRequest(
            customerId = customerId,
            restaurantId = restaurantId,
            items = items
        )

        // Then
        assertEquals(customerId, request.customerId)
        assertEquals(restaurantId, request.restaurantId)
        assertEquals(1, request.items.size)
        assertEquals(items[0].productId, request.items[0].productId)
        assertEquals(items[0].quantity, request.items[0].quantity)
        assertEquals(items[0].price, request.items[0].price)
    }
} 