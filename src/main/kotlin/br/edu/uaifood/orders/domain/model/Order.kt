package br.edu.uaifood.orders.domain.model

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "orders")
data class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),
    
    @Column(nullable = false)
    val customerId: String,
    
    @Column(nullable = false)
    val restaurantId: String,
    
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "order_id")
    val items: List<OrderItem>,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: OrderStatus = OrderStatus.CREATED,
    
    @Column(nullable = false)
    val totalAmount: Double = 0.0,
    
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @Column
    val customerCpf: String? = null
) {
    fun calculateTotal(): Double = items.sumOf { it.calculateSubtotal() }

    fun canBeUpdated(): Boolean = status != OrderStatus.DELIVERED && status != OrderStatus.CANCELLED

    fun updateStatus(newStatus: OrderStatus) {
        require(canBeUpdated()) { "Order cannot be updated in current status" }
        status = newStatus
        updatedAt = LocalDateTime.now()
    }

    fun nextStatus() {
        status = when (status) {
            OrderStatus.CREATED -> OrderStatus.PENDING_PAYMENT
            OrderStatus.PENDING_PAYMENT -> OrderStatus.PAYMENT_CONFIRMED
            OrderStatus.PAYMENT_CONFIRMED -> OrderStatus.IN_PREPARATION
            OrderStatus.IN_PREPARATION -> OrderStatus.READY_FOR_DELIVERY
            OrderStatus.READY_FOR_DELIVERY -> OrderStatus.DELIVERED
            else -> throw IllegalStateException("Order is already in final status")
        }
        updatedAt = LocalDateTime.now()
    }
}

@Entity
@Table(name = "order_items")
data class OrderItem(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),
    
    @Column(nullable = false)
    val productId: UUID,
    
    @Column(nullable = false)
    val quantity: Int,
    
    @Column(nullable = false)
    val price: Double
) {
    fun calculateSubtotal(): Double = quantity * price
}

enum class OrderStatus {
    CREATED,
    PENDING_PAYMENT,
    PAYMENT_CONFIRMED,
    PAID,
    PAYMENT_REJECTED,
    IN_PREPARATION,
    READY_FOR_DELIVERY,
    DELIVERED,
    CANCELLED;

    val priority: Int
        get() = when (this) {
            READY_FOR_DELIVERY -> 1
            IN_PREPARATION -> 2
            PAYMENT_CONFIRMED -> 3
            PAID -> 3
            PENDING_PAYMENT -> 4
            CREATED -> 5
            else -> 6
        }
} 