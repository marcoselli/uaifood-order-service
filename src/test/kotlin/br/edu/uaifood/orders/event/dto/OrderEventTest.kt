package br.edu.uaifood.orders.event.dto

import br.edu.uaifood.orders.domain.model.OrderStatus
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class OrderEventTest {

    @Test
    fun `OrderCreatedEvent should create instance with correct values`() {
        // Given
        val orderId = UUID.randomUUID()
        val customerId = "customer123"
        val restaurantId = "restaurant123"
        val status = OrderStatus.CREATED
        val items = listOf(
            OrderCreatedEvent.OrderItem(
                productId = UUID.randomUUID(),
                quantity = 2,
                price = 10.0
            )
        )

        // When
        val event = OrderCreatedEvent(
            orderId = orderId,
            customerId = customerId,
            restaurantId = restaurantId,
            status = status,
            items = items
        )

        // Then
        assertNotNull(event)
        assertEquals(orderId, event.orderId)
        assertEquals(customerId, event.customerId)
        assertEquals(restaurantId, event.restaurantId)
        assertEquals(status, event.status)
        assertEquals(items, event.items)
    }

    @Test
    fun `OrderPaymentRequestedEvent should create instance with correct values`() {
        // Given
        val orderId = UUID.randomUUID()
        val customerId = "customer123"
        val restaurantId = "restaurant123"
        val amount = 100.0
        val createdAt = LocalDateTime.now()

        // When
        val event = OrderPaymentRequestedEvent(
            orderId = orderId,
            customerId = customerId,
            restaurantId = restaurantId,
            amount = amount,
            createdAt = createdAt
        )

        // Then
        assertNotNull(event)
        assertEquals(orderId, event.orderId)
        assertEquals(customerId, event.customerId)
        assertEquals(restaurantId, event.restaurantId)
        assertEquals(amount, event.amount)
        assertEquals(createdAt, event.createdAt)
    }

    @Test
    fun `OrderStatusChangedEvent should create instance with correct values`() {
        // Given
        val orderId = UUID.randomUUID()
        val oldStatus = OrderStatus.CREATED
        val newStatus = OrderStatus.PENDING_PAYMENT

        // When
        val event = OrderStatusChangedEvent(
            orderId = orderId,
            oldStatus = oldStatus,
            newStatus = newStatus
        )

        // Then
        assertNotNull(event)
        assertEquals(orderId, event.orderId)
        assertEquals(oldStatus, event.oldStatus)
        assertEquals(newStatus, event.newStatus)
    }

    @Test
    fun `OrderPaymentStatusUpdatedEvent should create instance with correct values`() {
        // Given
        val orderId = UUID.randomUUID()
        val status = "APPROVED"
        val updatedAt = LocalDateTime.now()

        // When
        val event = OrderPaymentStatusUpdatedEvent(
            orderId = orderId,
            status = status,
            updatedAt = updatedAt
        )

        // Then
        assertNotNull(event)
        assertEquals(orderId, event.orderId)
        assertEquals(status, event.status)
        assertEquals(updatedAt, event.updatedAt)
    }
} 