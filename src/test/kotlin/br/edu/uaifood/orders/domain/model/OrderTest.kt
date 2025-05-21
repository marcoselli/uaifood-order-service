package br.edu.uaifood.orders.domain.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OrderTest {

    @Test
    fun `calculateTotal should return sum of all items subtotals`() {
        // Given
        val order = Order(
            customerId = "customer123",
            restaurantId = "restaurant456",
            items = listOf(
                OrderItem(productId = UUID.randomUUID(), quantity = 2, price = 10.0),
                OrderItem(productId = UUID.randomUUID(), quantity = 3, price = 15.0)
            )
        )

        // When
        val total = order.calculateTotal()

        // Then
        assertEquals(65.0, total) // (2 * 10.0) + (3 * 15.0) = 65.0
    }

    @Test
    fun `canBeUpdated should return true for updatable statuses`() {
        // Given
        val order = Order(
            customerId = "customer123",
            restaurantId = "restaurant456",
            items = emptyList(),
            status = OrderStatus.CREATED
        )

        // When/Then
        assertTrue(order.canBeUpdated())
    }

    @Test
    fun `canBeUpdated should return false for non-updatable statuses`() {
        // Given
        val deliveredOrder = Order(
            customerId = "customer123",
            restaurantId = "restaurant456",
            items = emptyList(),
            status = OrderStatus.DELIVERED
        )
        val cancelledOrder = Order(
            customerId = "customer123",
            restaurantId = "restaurant456",
            items = emptyList(),
            status = OrderStatus.CANCELLED
        )

        // When/Then
        assertFalse(deliveredOrder.canBeUpdated())
        assertFalse(cancelledOrder.canBeUpdated())
    }

    @Test
    fun `updateStatus should update status and timestamp when order can be updated`() {
        // Given
        val order = Order(
            customerId = "customer123",
            restaurantId = "restaurant456",
            items = emptyList(),
            status = OrderStatus.CREATED
        )
        val originalUpdatedAt = order.updatedAt

        // When
        order.updateStatus(OrderStatus.PENDING_PAYMENT)

        // Then
        assertEquals(OrderStatus.PENDING_PAYMENT, order.status)
        assertTrue(order.updatedAt.isAfter(originalUpdatedAt))
    }

    @Test
    fun `updateStatus should throw exception when order cannot be updated`() {
        // Given
        val order = Order(
            customerId = "customer123",
            restaurantId = "restaurant123",
            items = listOf(
                OrderItem(
                    productId = UUID.randomUUID(),
                    quantity = 2,
                    price = 10.0
                )
            ),
            status = OrderStatus.DELIVERED
        )

        // When/Then
        assertThrows<IllegalArgumentException> {
            order.updateStatus(OrderStatus.IN_PREPARATION)
        }
    }

    @Test
    fun `nextStatus should update status to next valid state`() {
        // Given
        val order = Order(
            customerId = "customer123",
            restaurantId = "restaurant456",
            items = emptyList(),
            status = OrderStatus.CREATED
        )

        // When
        order.nextStatus()

        // Then
        assertEquals(OrderStatus.PENDING_PAYMENT, order.status)
    }

    @Test
    fun `nextStatus should throw exception when order is in final status`() {
        // Given
        val order = Order(
            customerId = "customer123",
            restaurantId = "restaurant456",
            items = emptyList(),
            status = OrderStatus.DELIVERED
        )

        // When/Then
        assertThrows<IllegalStateException> {
            order.nextStatus()
        }
    }
} 