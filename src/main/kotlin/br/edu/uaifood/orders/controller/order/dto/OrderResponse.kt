package br.edu.uaifood.orders.controller.order.dto

import br.edu.uaifood.orders.domain.model.Order
import br.edu.uaifood.orders.domain.model.OrderItem
import br.edu.uaifood.orders.domain.model.OrderStatus
import java.util.UUID

data class OrderResponse(
    val id: UUID,
    val customerId: String,
    val restaurantId: String,
    val items: List<OrderItemResponse>,
    val status: OrderStatus,
    val totalAmount: Double,
    val createdAt: String,
    val updatedAt: String
) {
    companion object {
        fun from(order: Order): OrderResponse =
            OrderResponse(
                id = order.id,
                customerId = order.customerId,
                restaurantId = order.restaurantId,
                items = order.items.map { OrderItemResponse.from(it) },
                status = order.status,
                totalAmount = order.totalAmount,
                createdAt = order.createdAt.toString(),
                updatedAt = order.updatedAt.toString()
            )
    }
}

data class OrderItemResponse(
    val id: UUID,
    val productId: UUID,
    val quantity: Int,
    val price: Double,
    val subtotal: Double
) {
    companion object {
        fun from(item: OrderItem): OrderItemResponse =
            OrderItemResponse(
                id = item.id,
                productId = item.productId,
                quantity = item.quantity,
                price = item.price,
                subtotal = item.calculateSubtotal()
            )
    }
}


