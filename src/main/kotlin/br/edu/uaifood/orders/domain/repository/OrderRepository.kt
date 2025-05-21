package br.edu.uaifood.orders.domain.repository

import br.edu.uaifood.orders.domain.model.Order
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface OrderRepository : JpaRepository<Order, UUID> {
    fun findAllByOrderByStatusAscCreatedAtAsc(): List<Order>
    fun findByCustomerId(customerId: String): List<Order>
    fun findByRestaurantId(restaurantId: String): List<Order>
} 