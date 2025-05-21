package br.edu.uaifood.orders.usecase

import br.edu.uaifood.orders.domain.model.Order
import br.edu.uaifood.orders.domain.repository.OrderRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class FindAllOrdersUseCase(
    private val orderRepository: OrderRepository
) {
    @Transactional(readOnly = true)
    fun execute(): List<Order> {
        return orderRepository.findAll()
    }
}