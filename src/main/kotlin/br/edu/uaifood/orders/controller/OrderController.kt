package br.edu.uaifood.orders.controller

import br.edu.uaifood.orders.controller.order.dto.CreateOrderRequest
import br.edu.uaifood.orders.controller.order.dto.OrderResponse
import br.edu.uaifood.orders.domain.model.Order
import br.edu.uaifood.orders.domain.model.OrderItem
import br.edu.uaifood.orders.usecase.CreateOrderUseCase
import br.edu.uaifood.orders.usecase.FindAllOrdersUseCase
import br.edu.uaifood.orders.usecase.FindOrderByIdUseCase
import br.edu.uaifood.orders.usecase.UpdateOrderStatusUseCase
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/orders")
class OrderController(
    private val createOrderUseCase: CreateOrderUseCase,
    private val findAllOrdersUseCase: FindAllOrdersUseCase,
    private val findOrderByIdUseCase: FindOrderByIdUseCase,
    private val updateOrderStatusUseCase: UpdateOrderStatusUseCase
) {
    @PostMapping
    fun createOrder(@RequestBody request: CreateOrderRequest): ResponseEntity<OrderResponse> {
        val order = Order(
            customerId = request.customerId,
            restaurantId = request.restaurantId,
            items = request.items.map { item ->
                OrderItem(
                    productId = item.productId,
                    quantity = item.quantity,
                    price = item.price
                )
            }
        )
        val createdOrder = createOrderUseCase.execute(order)
        return ResponseEntity.ok(OrderResponse.from(createdOrder))
    }

    @GetMapping
    fun findAllOrders(): ResponseEntity<List<OrderResponse>> {
        val orders = findAllOrdersUseCase.execute()
        return ResponseEntity.ok(orders.map { OrderResponse.from(it) })
    }

    @GetMapping("/{orderId}")
    fun findOrderById(@PathVariable orderId: UUID): ResponseEntity<OrderResponse> {
        val order = findOrderByIdUseCase.execute(orderId)
        return ResponseEntity.ok(OrderResponse.from(order))
    }

    @PatchMapping("/{orderId}/status")
    fun updateOrderStatus(@PathVariable orderId: UUID): ResponseEntity<OrderResponse> {
        updateOrderStatusUseCase.execute(orderId)
        val updatedOrder = findOrderByIdUseCase.execute(orderId)
        return ResponseEntity.ok(OrderResponse.from(updatedOrder))
    }
} 