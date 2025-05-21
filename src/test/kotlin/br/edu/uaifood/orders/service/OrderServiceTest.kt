package br.edu.uaifood.orders.service

import br.edu.uaifood.orders.domain.model.Order
import br.edu.uaifood.orders.domain.model.OrderItem
import br.edu.uaifood.orders.domain.model.OrderStatus
import br.edu.uaifood.orders.domain.repository.OrderRepository
import br.edu.uaifood.orders.event.OrderEventPublisher
import br.edu.uaifood.orders.event.OrderPaymentRequestedPublisher
import br.edu.uaifood.orders.event.dto.OrderCreatedEvent
import br.edu.uaifood.orders.event.dto.OrderStatusChangedEvent
import br.edu.uaifood.orders.exception.OrderAlreadyFinishedException
import br.edu.uaifood.orders.exception.OrderNotFoundException
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OrderServiceTest {

    private val orderRepository: OrderRepository = mockk()
    private val eventPublisher: OrderEventPublisher = mockk()
    private val orderPaymentRequestedPublisher: OrderPaymentRequestedPublisher = mockk()
    private val orderService = OrderService(orderRepository, eventPublisher, orderPaymentRequestedPublisher)

    private lateinit var order: Order
    private lateinit var orderItem: OrderItem

    @BeforeEach
    fun setup() {
        clearAllMocks()
        
        orderItem = OrderItem(
            productId = UUID.randomUUID(),
            quantity = 2,
            price = 10.0
        )
        
        order = Order(
            customerId = "customer123",
            restaurantId = "restaurant456",
            items = listOf(orderItem),
            status = OrderStatus.CREATED,
            totalAmount = 20.0,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }

    @Test
    fun `should find all orders sorted by status priority and creation date`() {
        // Given
        val order1 = order.copy(
            id = UUID.randomUUID(),
            status = OrderStatus.READY_FOR_DELIVERY,
            createdAt = LocalDateTime.now().minusHours(1)
        )
        val order2 = order.copy(
            id = UUID.randomUUID(),
            status = OrderStatus.IN_PREPARATION,
            createdAt = LocalDateTime.now()
        )
        val order3 = order.copy(
            id = UUID.randomUUID(),
            status = OrderStatus.DELIVERED,
            createdAt = LocalDateTime.now().minusMinutes(30)
        )

        every { orderRepository.findAllByOrderByStatusAscCreatedAtAsc() } returns listOf(order1, order2, order3)

        // When
        val result = orderService.findAllOrders()

        // Then
        assertEquals(3, result.size)
        assertEquals(OrderStatus.READY_FOR_DELIVERY, result[0].status)
        assertEquals(OrderStatus.IN_PREPARATION, result[1].status)
        assertEquals(OrderStatus.DELIVERED, result[2].status)
        verify { orderRepository.findAllByOrderByStatusAscCreatedAtAsc() }
    }

    @Test
    fun `should create order and publish event`() {
        // Given
        val savedOrder = order.copy(id = UUID.randomUUID())
        every { orderRepository.save(any()) } returns savedOrder
        every { eventPublisher.publishOrderCreated(any()) } just Runs
        every { orderPaymentRequestedPublisher.publish(any()) } just Runs

        // When
        val result = orderService.createOrder(order)

        // Then
        assertEquals(savedOrder.id, result.id)
        assertEquals(savedOrder.customerId, result.customerId)
        assertEquals(savedOrder.restaurantId, result.restaurantId)
        assertEquals(1, result.items.size)
        assertEquals(orderItem.productId, result.items[0].productId)
        assertEquals(orderItem.quantity, result.items[0].quantity)
        assertEquals(orderItem.price, result.items[0].price)

        verify { 
            orderRepository.save(order)
            eventPublisher.publishOrderCreated(match { event ->
                event.orderId == savedOrder.id &&
                event.customerId == savedOrder.customerId &&
                event.restaurantId == savedOrder.restaurantId &&
                event.status == savedOrder.status &&
                event.items.size == 1 &&
                event.items[0].productId == orderItem.productId &&
                event.items[0].quantity == orderItem.quantity &&
                event.items[0].price == orderItem.price
            })
            orderPaymentRequestedPublisher.publish(savedOrder)
        }
    }

    @Test
    fun `should update order status and publish event`() {
        // Given
        val orderId = UUID.randomUUID()
        val orderToUpdate = order.copy(
            id = orderId,
            status = OrderStatus.CREATED
        )
        val updatedOrder = orderToUpdate.copy(
            status = OrderStatus.PENDING_PAYMENT,
            updatedAt = LocalDateTime.now()
        )

        every { orderRepository.findById(orderId) } returns Optional.of(orderToUpdate)
        every { orderRepository.save(any()) } returns updatedOrder
        every { eventPublisher.publishOrderStatusChanged(any()) } just Runs

        // When
        orderService.updateOrderStatus(orderId)

        // Then
        verify { 
            orderRepository.findById(orderId)
            orderRepository.save(match { it.status == OrderStatus.PENDING_PAYMENT })
            eventPublisher.publishOrderStatusChanged(match { event ->
                event.orderId == orderId &&
                event.oldStatus == OrderStatus.CREATED &&
                event.newStatus == OrderStatus.PENDING_PAYMENT
            })
        }
    }

    @Test
    fun `should throw OrderNotFoundException when order not found`() {
        // Given
        val orderId = UUID.randomUUID()
        every { orderRepository.findById(orderId) } returns Optional.empty()

        // When/Then
        val exception = assertThrows<OrderNotFoundException> {
            orderService.updateOrderStatus(orderId)
        }
        assertEquals("Order not found with id: $orderId", exception.message)
        verify { orderRepository.findById(orderId) }
        verify(exactly = 0) { orderRepository.save(any()) }
        verify(exactly = 0) { eventPublisher.publishOrderStatusChanged(any()) }
    }

    @Test
    fun `should throw OrderAlreadyFinishedException when order is in final status`() {
        // Given
        val orderId = UUID.randomUUID()
        val finishedOrder = order.copy(
            id = orderId,
            status = OrderStatus.DELIVERED
        )
        every { orderRepository.findById(orderId) } returns Optional.of(finishedOrder)

        // When/Then
        val exception = assertThrows<OrderAlreadyFinishedException> {
            orderService.updateOrderStatus(orderId)
        }
        assertEquals("Order is already in a final status", exception.message)
        verify { orderRepository.findById(orderId) }
        verify(exactly = 0) { orderRepository.save(any()) }
        verify(exactly = 0) { eventPublisher.publishOrderStatusChanged(any()) }
    }

    @Test
    fun `should sort orders by priority and filter out delivered orders`() {
        // Given
        val order1 = order.copy(
            id = UUID.randomUUID(),
            status = OrderStatus.READY_FOR_DELIVERY,
            createdAt = LocalDateTime.now().minusHours(1)
        )
        val order2 = order.copy(
            id = UUID.randomUUID(),
            status = OrderStatus.IN_PREPARATION,
            createdAt = LocalDateTime.now()
        )
        val order3 = order.copy(
            id = UUID.randomUUID(),
            status = OrderStatus.DELIVERED,
            createdAt = LocalDateTime.now().minusMinutes(30)
        )
        val order4 = order.copy(
            id = UUID.randomUUID(),
            status = OrderStatus.PAYMENT_CONFIRMED,
            createdAt = LocalDateTime.now().minusMinutes(15)
        )

        val orders = listOf(order1, order2, order3, order4)

        // When
        val result = orderService.retrieveOrdersSortedByPriority(orders)

        // Then
        assertEquals(3, result.size)
        assertTrue(result.none { it.status == OrderStatus.DELIVERED })
        assertEquals(OrderStatus.READY_FOR_DELIVERY, result[0].status)
        assertEquals(OrderStatus.IN_PREPARATION, result[1].status)
        assertEquals(OrderStatus.PAYMENT_CONFIRMED, result[2].status)
    }
} 