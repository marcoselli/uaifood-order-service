package br.edu.uaifood.orders.event.dto

import java.time.LocalDateTime
import java.util.*

data class OrderPaymentStatusUpdatedEvent(
    val orderId: UUID,
    val status: String, // APPROVED, REJECTED, CANCELLED
    val updatedAt: LocalDateTime = LocalDateTime.now()
) 