package br.edu.uaifood.orders.event.dto

import br.edu.uaifood.orders.domain.model.OrderStatus
import java.util.UUID

data class OrderCreatedEvent(
    val orderId: UUID,
    val customerId: String,
    val restaurantId: String,
    val status: OrderStatus,
    val items: List<OrderItem>
) {
    data class OrderItem(
        val productId: UUID,
        val quantity: Int,
        val price: Double
    )
} 