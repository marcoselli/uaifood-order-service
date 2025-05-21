// uaifood-order-service/src/main/kotlin/br/edu/uaifood/orders/usecase/CreateOrderUseCase.kt
package br.edu.uaifood.orders.usecase

import br.edu.uaifood.orders.domain.model.Order
import br.edu.uaifood.orders.domain.repository.OrderRepository
import br.edu.uaifood.orders.event.OrderEventPublisher
import br.edu.uaifood.orders.event.dto.OrderCreatedEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class CreateOrderUseCase(
    private val orderRepository: OrderRepository,
    private val eventPublisher: OrderEventPublisher
) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    @Transactional
    fun execute(order: Order): Order {
        require(order.items.isNotEmpty()) { "Order must contain at least one item" }
        
        val savedOrder = orderRepository.save(order)

        eventPublisher.publishOrderCreated(
            OrderCreatedEvent(
                orderId = savedOrder.id,
                customerId = savedOrder.customerId,
                restaurantId = savedOrder.restaurantId,
                status = savedOrder.status,
                items = savedOrder.items.map { item ->
                    OrderCreatedEvent.OrderItem(
                        productId = item.productId,
                        quantity = item.quantity,
                        price = item.price
                    )
                }
            )
        )

        logger.info("Order created successfully with id: ${savedOrder.id}")
        return savedOrder
    }
}