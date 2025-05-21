package br.edu.uaifood.orders.usecase

import br.edu.uaifood.orders.domain.model.Order
import br.edu.uaifood.orders.domain.model.OrderItem
import br.edu.uaifood.orders.domain.model.OrderStatus
import br.edu.uaifood.orders.domain.repository.OrderRepository
import br.edu.uaifood.orders.event.OrderEventPublisher
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

class UpdateOrderStatusUseCaseTest {

    private val orderRepository: OrderRepository = mockk()
    private val eventPublisher: OrderEventPublisher = mockk()
    private val updateOrderStatusUseCase = UpdateOrderStatusUseCase(orderRepository, eventPublisher)

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
    fun `should update order status and publish event successfully`() {
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
        updateOrderStatusUseCase.execute(orderId)

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
            updateOrderStatusUseCase.execute(orderId)
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
            updateOrderStatusUseCase.execute(orderId)
        }
        assertEquals("Order is already in a final status", exception.message)
        verify { orderRepository.findById(orderId) }
        verify(exactly = 0) { orderRepository.save(any()) }
        verify(exactly = 0) { eventPublisher.publishOrderStatusChanged(any()) }
    }

    @Test
    fun `should follow correct status transition sequence`() {
        // Given
        val orderId = UUID.randomUUID()
        val statusSequence = listOf(
            OrderStatus.CREATED,
            OrderStatus.PENDING_PAYMENT,
            OrderStatus.PAYMENT_CONFIRMED,
            OrderStatus.IN_PREPARATION,
            OrderStatus.READY_FOR_DELIVERY,
            OrderStatus.DELIVERED
        )

        var currentOrder = order.copy(id = orderId, status = statusSequence[0])
        
        for (i in 0 until statusSequence.size - 1) {
            val currentStatus = statusSequence[i]
            val nextStatus = statusSequence[i + 1]
            
            every { orderRepository.findById(orderId) } returns Optional.of(currentOrder)
            every { orderRepository.save(any()) } answers {
                currentOrder = currentOrder.copy(
                    status = nextStatus,
                    updatedAt = LocalDateTime.now()
                )
                currentOrder
            }
            every { eventPublisher.publishOrderStatusChanged(any()) } just Runs

            // When
            updateOrderStatusUseCase.execute(orderId)

            // Then
            verify { 
                orderRepository.findById(orderId)
                orderRepository.save(match { it.status == nextStatus })
                eventPublisher.publishOrderStatusChanged(match { event ->
                    event.orderId == orderId &&
                    event.oldStatus == currentStatus &&
                    event.newStatus == nextStatus
                })
            }

            clearAllMocks()
        }

        // Verify that trying to update a delivered order throws exception
        every { orderRepository.findById(orderId) } returns Optional.of(currentOrder)
        val exception = assertThrows<OrderAlreadyFinishedException> {
            updateOrderStatusUseCase.execute(orderId)
        }
        assertEquals("Order is already in a final status", exception.message)
    }
} 