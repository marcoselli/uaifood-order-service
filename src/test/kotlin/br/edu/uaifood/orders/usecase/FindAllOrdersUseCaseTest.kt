package br.edu.uaifood.orders.usecase

import br.edu.uaifood.orders.domain.model.Order
import br.edu.uaifood.orders.domain.model.OrderItem
import br.edu.uaifood.orders.domain.model.OrderStatus
import br.edu.uaifood.orders.domain.repository.OrderRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FindAllOrdersUseCaseTest {
    private val orderRepository: OrderRepository = mockk()
    private val findAllOrdersUseCase = FindAllOrdersUseCase(orderRepository)

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
    fun `should return all orders`() {
        // Given
        val order1 = order.copy(id = UUID.randomUUID())
        val order2 = order.copy(id = UUID.randomUUID())
        val order3 = order.copy(id = UUID.randomUUID())
        
        every { orderRepository.findAll() } returns listOf(order1, order2, order3)

        // When
        val result = findAllOrdersUseCase.execute()

        // Then
        assertEquals(3, result.size)
        assertTrue(result.contains(order1))
        assertTrue(result.contains(order2))
        assertTrue(result.contains(order3))
        verify { orderRepository.findAll() }
    }

    @Test
    fun `should return empty list when no orders exist`() {
        // Given
        every { orderRepository.findAll() } returns emptyList()

        // When
        val result = findAllOrdersUseCase.execute()

        // Then
        assertTrue(result.isEmpty())
        verify { orderRepository.findAll() }
    }
}