package br.edu.uaifood.orders.domain.model

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class OrderStatusTest {

    @Test
    fun `priority should return correct value for each status`() {
        // Given/When/Then
        assertEquals(1, OrderStatus.READY_FOR_DELIVERY.priority)
        assertEquals(2, OrderStatus.IN_PREPARATION.priority)
        assertEquals(3, OrderStatus.PAYMENT_CONFIRMED.priority)
        assertEquals(3, OrderStatus.PAID.priority)
        assertEquals(4, OrderStatus.PENDING_PAYMENT.priority)
        assertEquals(5, OrderStatus.CREATED.priority)
        assertEquals(6, OrderStatus.PAYMENT_REJECTED.priority)
        assertEquals(6, OrderStatus.DELIVERED.priority)
        assertEquals(6, OrderStatus.CANCELLED.priority)
    }
} 