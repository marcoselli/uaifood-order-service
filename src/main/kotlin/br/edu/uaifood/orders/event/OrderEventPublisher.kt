package br.edu.uaifood.orders.event

import br.edu.uaifood.orders.config.RabbitMQConfig
import br.edu.uaifood.orders.event.dto.OrderCreatedEvent
import br.edu.uaifood.orders.event.dto.OrderStatusChangedEvent
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component

@Component
class OrderEventPublisher(
    private val rabbitTemplate: RabbitTemplate
) {
    fun publishOrderCreated(event: OrderCreatedEvent) {
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.ORDER_EXCHANGE,
            RabbitMQConfig.ORDER_CREATED_ROUTING_KEY,
            event
        )
    }

    fun publishOrderStatusChanged(event: OrderStatusChangedEvent) {
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.ORDER_EXCHANGE,
            RabbitMQConfig.ORDER_STATUS_UPDATED_ROUTING_KEY,
            event
        )
    }
} 