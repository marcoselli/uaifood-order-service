package br.edu.uaifood.orders.usecase

import br.edu.uaifood.orders.domain.model.Order
import br.edu.uaifood.orders.domain.model.OrderItem
import br.edu.uaifood.orders.domain.model.OrderStatus
import br.edu.uaifood.orders.domain.repository.OrderRepository
import br.edu.uaifood.orders.exception.OrderNotFoundException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals

class FindOrderByIdUseCaseTest {

    private val orderRepository: OrderRepository = mockk()
    private val findOrderByIdUseCase = FindOrderByIdUseCase(orderRepository)

    private lateinit var order: Order
    private lateinit var orderItem: OrderItem

    @BeforeEach
    fun setup() {
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
    fun `should return order when found by id`() {
        // Given
        val orderId = UUID.randomUUID()
        val orderToFind = order.copy(id = orderId)
        every { orderRepository.findById(orderId) } returns Optional.of(orderToFind)

        // When
        val result = findOrderByIdUseCase.execute(orderId)

        // Then
        assertEquals(orderId, result.id)
        assertEquals(orderToFind.customerId, result.customerId)
        assertEquals(orderToFind.restaurantId, result.restaurantId)
        assertEquals(1, result.items.size)
        assertEquals(orderItem.productId, result.items[0].productId)
        assertEquals(orderItem.quantity, result.items[0].quantity)
        assertEquals(orderItem.price, result.items[0].price)
        verify { orderRepository.findById(orderId) }
    }

    @Test
    fun `should throw OrderNotFoundException when order not found`() {
        // Given
        val orderId = UUID.randomUUID()
        every { orderRepository.findById(orderId) } returns Optional.empty()

        // When/Then
        val exception = assertThrows<OrderNotFoundException> {
            findOrderByIdUseCase.execute(orderId)
        }
        assertEquals("Order not found with id: $orderId", exception.message)
        verify { orderRepository.findById(orderId) }
    }
} 