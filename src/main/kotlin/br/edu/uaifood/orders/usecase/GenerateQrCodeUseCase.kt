package br.edu.uaifood.orders.usecase

import br.edu.uaifood.orders.domain.model.Order
import org.springframework.stereotype.Component

@Component
class GenerateQrCodeUseCase {
    fun execute(order: Order): String = "qrFake"
}