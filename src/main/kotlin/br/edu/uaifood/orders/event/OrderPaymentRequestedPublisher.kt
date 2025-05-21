package br.edu.uaifood.orders.event

import br.edu.uaifood.orders.domain.model.Order
import br.edu.uaifood.orders.event.dto.OrderPaymentRequestedEvent
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class OrderPaymentRequestedPublisher(
    private val kafkaTemplate: KafkaTemplate<String, OrderPaymentRequestedEvent>
) {
    fun publish(order: Order) {
        val event = OrderPaymentRequestedEvent(
            orderId = order.id,
            customerId = order.customerId,
            restaurantId = order.restaurantId,
            amount = order.totalAmount
        )
        kafkaTemplate.send("order-payment-requested", order.id.toString(), event)
    }
} 