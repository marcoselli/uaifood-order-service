package br.edu.uaifood.orders.event.dto

import br.edu.uaifood.orders.domain.model.OrderStatus
import java.util.UUID

data class OrderStatusChangedEvent(
    val orderId: UUID,
    val oldStatus: OrderStatus,
    val newStatus: OrderStatus
) 