package br.edu.uaifood.orders.usecase

import br.edu.uaifood.orders.domain.model.Order
import br.edu.uaifood.orders.domain.repository.OrderRepository
import br.edu.uaifood.orders.exception.OrderNotFoundException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class FindOrderByIdUseCase(
    private val orderRepository: OrderRepository
) {
    @Transactional(readOnly = true)
    fun execute(orderId: UUID): Order {
        return orderRepository.findById(orderId).orElseThrow {
            OrderNotFoundException("Order not found with id: $orderId")
        }
    }
} 