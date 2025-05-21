package br.edu.uaifood.orders.bdd

import br.edu.uaifood.orders.domain.model.Order
import br.edu.uaifood.orders.domain.model.OrderItem
import br.edu.uaifood.orders.domain.model.OrderStatus
import br.edu.uaifood.orders.domain.repository.OrderRepository
import br.edu.uaifood.orders.event.OrderEventPublisher
import br.edu.uaifood.orders.usecase.CreateOrderUseCase
import io.cucumber.datatable.DataTable
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertNull

class OrderCreationSteps {
    private val orderRepository: OrderRepository = mockk()
    private val eventPublisher: OrderEventPublisher = mockk()
    private val createOrderUseCase = CreateOrderUseCase(orderRepository, eventPublisher)
    
    private var customerId: String? = null
    private var restaurantId: String? = null
    private var orderItems: MutableList<OrderItem> = mutableListOf()
    private var createdOrder: Order? = null
    private var exception: Exception? = null

    @Given("I am a customer with ID {string}")
    fun iAmACustomerWithId(customerId: String) {
        this.customerId = customerId
    }

    @Given("I want to order from restaurant {string}")
    fun iWantToOrderFromRestaurant(restaurantId: String) {
        this.restaurantId = restaurantId
    }

    @When("I add the following items to my order:")
    fun iAddTheFollowingItemsToMyOrder(itemsTable: DataTable) {
        itemsTable.asMaps().forEach { row ->
            orderItems.add(
                OrderItem(
                    productId = UUID.fromString(row["Product ID"]),
                    quantity = row["Quantity"]?.toInt() ?: 0,
                    price = row["Price"]?.toDouble() ?: 0.0
                )
            )
        }
    }

    @When("I submit my order")
    fun iSubmitMyOrder() {
        try {
            val order = Order(
                customerId = customerId!!,
                restaurantId = restaurantId!!,
                items = orderItems,
                status = OrderStatus.CREATED,
                totalAmount = orderItems.sumOf { it.price * it.quantity },
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )

            every { orderRepository.save(any()) } returns order.copy(id = UUID.randomUUID())
            every { eventPublisher.publishOrderCreated(any()) } just Runs

            createdOrder = createOrderUseCase.execute(order)

            verify { 
                orderRepository.save(any())
                eventPublisher.publishOrderCreated(any())
            }
        } catch (e: Exception) {
            exception = e
        }
    }

    @When("I submit my order without any items")
    fun iSubmitMyOrderWithoutAnyItems() {
        try {
            val order = Order(
                customerId = customerId!!,
                restaurantId = restaurantId!!,
                items = emptyList(),
                status = OrderStatus.CREATED,
                totalAmount = 0.0,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )

            createdOrder = createOrderUseCase.execute(order)
        } catch (e: Exception) {
            exception = e
        }
    }

    @Then("my order should be created successfully")
    fun myOrderShouldBeCreatedSuccessfully() {
        assertNotNull(createdOrder)
        assertNull(exception)
    }

    @Then("the order status should be {string}")
    fun theOrderStatusShouldBe(status: String) {
        assertEquals(OrderStatus.valueOf(status), createdOrder?.status)
    }

    @Then("the total amount should be {double}")
    fun theTotalAmountShouldBe(amount: Double) {
        assertEquals(amount, createdOrder?.totalAmount)
    }

    @Then("an order created event should be published")
    fun anOrderCreatedEventShouldBePublished() {
        verify { eventPublisher.publishOrderCreated(any()) }
    }

    @Then("the order creation should fail")
    fun theOrderCreationShouldFail() {
        assertNotNull(exception)
    }

    @Then("I should receive an error message about empty order")
    fun iShouldReceiveAnErrorMessageAboutEmptyOrder() {
        assertEquals("Order must contain at least one item", exception?.message)
    }
} 