package br.edu.uaifood.orders.controller

import br.edu.uaifood.orders.controller.order.dto.CreateOrderRequest
import br.edu.uaifood.orders.controller.order.dto.OrderItemRequest
import br.edu.uaifood.orders.controller.order.dto.OrderResponse
import br.edu.uaifood.orders.domain.model.Order
import br.edu.uaifood.orders.domain.model.OrderItem
import br.edu.uaifood.orders.domain.model.OrderStatus
import br.edu.uaifood.orders.usecase.CreateOrderUseCase
import br.edu.uaifood.orders.usecase.FindAllOrdersUseCase
import br.edu.uaifood.orders.usecase.FindOrderByIdUseCase
import br.edu.uaifood.orders.usecase.UpdateOrderStatusUseCase
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OrderControllerTest {
    private val createOrderUseCase: CreateOrderUseCase = mockk()
    private val findAllOrdersUseCase: FindAllOrdersUseCase = mockk()
    private val findOrderByIdUseCase: FindOrderByIdUseCase = mockk()
    private val updateOrderStatusUseCase: UpdateOrderStatusUseCase = mockk()
    
    private val orderController = OrderController(
        createOrderUseCase,
        findAllOrdersUseCase,
        findOrderByIdUseCase,
        updateOrderStatusUseCase
    )

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
    fun `should create order successfully`() {
        // Given
        val request = CreateOrderRequest(
            customerId = "customer123",
            restaurantId = "restaurant456",
            items = listOf(
                OrderItemRequest(
                    productId = orderItem.productId,
                    quantity = orderItem.quantity,
                    price = orderItem.price
                )
            )
        )
        val savedOrder = order.copy(id = UUID.randomUUID())
        
        every { createOrderUseCase.execute(any()) } returns savedOrder

        // When
        val response = orderController.createOrder(request)

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        val responseBody = response.body!!
        assertEquals(savedOrder.id, responseBody.id)
        assertEquals(savedOrder.customerId, responseBody.customerId)
        assertEquals(savedOrder.restaurantId, responseBody.restaurantId)
        assertEquals(1, responseBody.items.size)
        assertEquals(orderItem.productId, responseBody.items[0].productId)
        assertEquals(orderItem.quantity, responseBody.items[0].quantity)
        assertEquals(orderItem.price, responseBody.items[0].price)
        verify { createOrderUseCase.execute(any()) }
    }

    @Test
    fun `should find all orders`() {
        // Given
        val order1 = order.copy(id = UUID.randomUUID())
        val order2 = order.copy(id = UUID.randomUUID())
        
        every { findAllOrdersUseCase.execute() } returns listOf(order1, order2)

        // When
        val response = orderController.findAllOrders()

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        val responseBody = response.body!!
        assertEquals(2, responseBody.size)
        assertTrue(responseBody.any { it.id == order1.id })
        assertTrue(responseBody.any { it.id == order2.id })
        verify { findAllOrdersUseCase.execute() }
    }

    @Test
    fun `should find order by id`() {
        // Given
        val orderId = UUID.randomUUID()
        val orderToFind = order.copy(id = orderId)
        
        every { findOrderByIdUseCase.execute(orderId) } returns orderToFind

        // When
        val response = orderController.findOrderById(orderId)

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        val responseBody = response.body!!
        assertEquals(orderId, responseBody.id)
        assertEquals(orderToFind.customerId, responseBody.customerId)
        assertEquals(orderToFind.restaurantId, responseBody.restaurantId)
        assertEquals(1, responseBody.items.size)
        assertEquals(orderItem.productId, responseBody.items[0].productId)
        assertEquals(orderItem.quantity, responseBody.items[0].quantity)
        assertEquals(orderItem.price, responseBody.items[0].price)
        verify { findOrderByIdUseCase.execute(orderId) }
    }

    @Test
    fun `should update order status`() {
        // Given
        val orderId = UUID.randomUUID()
        val updatedOrder = order.copy(
            id = orderId,
            status = OrderStatus.PENDING_PAYMENT
        )
        
        every { updateOrderStatusUseCase.execute(orderId) } just Runs
        every { findOrderByIdUseCase.execute(orderId) } returns updatedOrder

        // When
        val response = orderController.updateOrderStatus(orderId)

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        val responseBody = response.body!!
        assertEquals(orderId, responseBody.id)
        assertEquals(OrderStatus.PENDING_PAYMENT, responseBody.status)
        verify { 
            updateOrderStatusUseCase.execute(orderId)
            findOrderByIdUseCase.execute(orderId)
        }
    }
} 