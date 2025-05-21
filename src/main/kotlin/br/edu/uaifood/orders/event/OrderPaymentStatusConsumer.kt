package br.edu.uaifood.orders.event

import br.edu.uaifood.orders.domain.model.OrderStatus
import br.edu.uaifood.orders.domain.repository.OrderRepository
import br.edu.uaifood.orders.event.dto.OrderPaymentStatusUpdatedEvent
import br.edu.uaifood.orders.exception.OrderNotFoundException
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class OrderPaymentStatusConsumer(
    private val orderRepository: OrderRepository
) {
    @KafkaListener(topics = ["order-payment-status-updated"], groupId = "order-service")
    fun consume(event: OrderPaymentStatusUpdatedEvent) {
        val order = orderRepository.findById(event.orderId)
            .orElseThrow { OrderNotFoundException("Order not found with id: ${event.orderId}") }

        val newStatus = when (event.status) {
            "APPROVED" -> OrderStatus.PAID
            "REJECTED" -> OrderStatus.PAYMENT_REJECTED
            "CANCELLED" -> OrderStatus.CANCELLED
            else -> throw IllegalArgumentException("Invalid payment status: ${event.status}")
        }

        val updatedOrder = order.copy(status = newStatus)
        orderRepository.save(updatedOrder)
    }
} 