package br.edu.uaifood.orders.usecase

import br.edu.uaifood.orders.domain.model.Order
import br.edu.uaifood.orders.domain.model.OrderItem
import br.edu.uaifood.orders.domain.model.OrderStatus
import br.edu.uaifood.orders.domain.repository.OrderRepository
import br.edu.uaifood.orders.event.OrderEventPublisher
import br.edu.uaifood.orders.event.dto.OrderCreatedEvent
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals

class CreateOrderUseCaseTest {

    private val orderRepository: OrderRepository = mockk()
    private val eventPublisher: OrderEventPublisher = mockk()
    private val createOrderUseCase = CreateOrderUseCase(orderRepository, eventPublisher)

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
    fun `should create order and publish event successfully`() {
        // Given
        val savedOrder = order.copy(id = UUID.randomUUID())
        every { orderRepository.save(any()) } returns savedOrder
        every { eventPublisher.publishOrderCreated(any()) } just Runs

        // When
        val result = createOrderUseCase.execute(order)

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
        }
    }

    @Test
    fun `should calculate total amount correctly`() {
        // Given
        val item1 = OrderItem(
            productId = UUID.randomUUID(),
            quantity = 2,
            price = 10.0
        )
        val item2 = OrderItem(
            productId = UUID.randomUUID(),
            quantity = 3,
            price = 5.0
        )
        val orderWithMultipleItems = order.copy(
            items = listOf(item1, item2),
            totalAmount = 0.0 // Will be calculated
        )
        val savedOrder = orderWithMultipleItems.copy(
            id = UUID.randomUUID(),
            totalAmount = 35.0 // 2 * 10.0 + 3 * 5.0
        )

        every { orderRepository.save(any()) } returns savedOrder
        every { eventPublisher.publishOrderCreated(any()) } just Runs

        // When
        val result = createOrderUseCase.execute(orderWithMultipleItems)

        // Then
        assertEquals(35.0, result.totalAmount)
        verify { 
            orderRepository.save(match { it.calculateTotal() == 35.0 })
            eventPublisher.publishOrderCreated(any())
        }
    }
} 