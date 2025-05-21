package br.edu.uaifood.orders.controller.order.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.util.UUID

data class CreateOrderRequest(
    @field:NotBlank
    val customerId: String,

    @field:NotBlank
    val restaurantId: String,

    @field:NotEmpty
    @field:Valid
    val items: List<OrderItemRequest>
)

data class OrderItemRequest(
    @field:NotNull
    val productId: UUID,

    @field:NotNull
    @field:Min(1)
    val quantity: Int,

    @field:NotNull
    @field:Min(0)
    val price: Double
) 