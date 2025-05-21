package br.edu.uaifood.orders.event.dto

import java.time.LocalDateTime
import java.util.*

data class OrderPaymentRequestedEvent(
    val orderId: UUID,
    val customerId: String,
    val restaurantId: String,
    val amount: Double,
    val createdAt: LocalDateTime = LocalDateTime.now()
) 