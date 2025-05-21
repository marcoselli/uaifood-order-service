package br.edu.uaifood.orders.usecase

import br.edu.uaifood.orders.domain.model.Order
import br.edu.uaifood.orders.domain.repository.OrderRepository
import br.edu.uaifood.orders.event.OrderEventPublisher
import br.edu.uaifood.orders.event.dto.OrderStatusChangedEvent
import br.edu.uaifood.orders.exception.OrderAlreadyFinishedException
import br.edu.uaifood.orders.exception.OrderNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class UpdateOrderStatusUseCase(
    private val orderRepository: OrderRepository,
    private val eventPublisher: OrderEventPublisher
) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    @Transactional
    fun execute(orderId: UUID) {
        val order = orderRepository.findById(orderId).orElseThrow {
            OrderNotFoundException("Order not found with id: $orderId")
        }

        if (!order.canBeUpdated()) {
            throw OrderAlreadyFinishedException("Order is already in a final status")
        }

        val oldStatus = order.status
        order.nextStatus()
        val updatedOrder = orderRepository.save(order)

        eventPublisher.publishOrderStatusChanged(
            OrderStatusChangedEvent(
                orderId = updatedOrder.id,
                oldStatus = oldStatus,
                newStatus = updatedOrder.status
            )
        )

            logger.info("Order id $orderId status updated successfully")
    }
}