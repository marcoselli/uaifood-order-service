package br.edu.uaifood.orders.service

import br.edu.uaifood.orders.domain.model.Order
import br.edu.uaifood.orders.domain.model.OrderStatus
import br.edu.uaifood.orders.domain.repository.OrderRepository
import br.edu.uaifood.orders.event.OrderEventPublisher
import br.edu.uaifood.orders.event.OrderPaymentRequestedPublisher
import br.edu.uaifood.orders.event.dto.OrderCreatedEvent
import br.edu.uaifood.orders.event.dto.OrderStatusChangedEvent
import br.edu.uaifood.orders.exception.OrderAlreadyFinishedException
import br.edu.uaifood.orders.exception.OrderNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val eventPublisher: OrderEventPublisher,
    private val orderPaymentRequestedPublisher: OrderPaymentRequestedPublisher
) {
    @Transactional(readOnly = true)
    fun findAllOrders(): List<Order> =
        orderRepository.findAllByOrderByStatusAscCreatedAtAsc()

    @Transactional
    fun createOrder(order: Order): Order {
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
        orderPaymentRequestedPublisher.publish(savedOrder)
        return savedOrder
    }

    @Transactional
    fun updateOrderStatus(orderId: UUID) {
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
    }

    fun retrieveOrdersSortedByPriority(orders: List<Order>): List<Order> =
        orders.filterNot { it.status == OrderStatus.DELIVERED }
            .sortedWith(compareBy<Order> { it.status.priority }.thenBy { it.createdAt })

    @Transactional(readOnly = true)
    fun findOrderById(id: UUID): Order {
        return orderRepository.findById(id)
            .orElseThrow { OrderNotFoundException("Order not found with id: $id") }
    }

    @Transactional(readOnly = true)
    fun findOrdersByCustomerId(customerId: String): List<Order> {
        return orderRepository.findByCustomerId(customerId)
    }

    @Transactional(readOnly = true)
    fun findOrdersByRestaurantId(restaurantId: String): List<Order> {
        return orderRepository.findByRestaurantId(restaurantId)
    }

    @Transactional
    fun updateOrderStatus(id: UUID, status: OrderStatus): Order {
        val order = findOrderById(id)
        val updatedOrder = order.copy(status = status)
        return orderRepository.save(updatedOrder)
    }

    @Transactional
    fun cancelOrder(id: UUID): Order {
        val order = findOrderById(id)
        if (order.status == OrderStatus.PAID) {
            throw IllegalStateException("Cannot cancel a paid order")
        }
        val updatedOrder = order.copy(status = OrderStatus.CANCELLED)
        return orderRepository.save(updatedOrder)
    }
}