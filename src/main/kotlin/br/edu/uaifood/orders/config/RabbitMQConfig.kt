package br.edu.uaifood.orders.config

import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitMQConfig {

    companion object {
        const val ORDER_EXCHANGE = "order.exchange"
        const val ORDER_CREATED_QUEUE = "order.created.queue"
        const val ORDER_STATUS_UPDATED_QUEUE = "order.status.updated.queue"
        const val ORDER_CREATED_ROUTING_KEY = "order.created"
        const val ORDER_STATUS_UPDATED_ROUTING_KEY = "order.status.updated"
    }

    @Bean
    fun orderExchange(): DirectExchange = DirectExchange(ORDER_EXCHANGE)

    @Bean
    fun orderCreatedQueue(): Queue = Queue(ORDER_CREATED_QUEUE)

    @Bean
    fun orderStatusUpdatedQueue(): Queue = Queue(ORDER_STATUS_UPDATED_QUEUE)

    @Bean
    fun orderCreatedBinding(): Binding = BindingBuilder
        .bind(orderCreatedQueue())
        .to(orderExchange())
        .with(ORDER_CREATED_ROUTING_KEY)

    @Bean
    fun orderStatusUpdatedBinding(): Binding = BindingBuilder
        .bind(orderStatusUpdatedQueue())
        .to(orderExchange())
        .with(ORDER_STATUS_UPDATED_ROUTING_KEY)

    @Bean
    fun rabbitTemplate(connectionFactory: ConnectionFactory): RabbitTemplate {
        val rabbitTemplate = RabbitTemplate(connectionFactory)
        rabbitTemplate.messageConverter = Jackson2JsonMessageConverter()
        return rabbitTemplate
    }
} 