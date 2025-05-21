package br.edu.uaifood.orders.event

import br.edu.uaifood.orders.config.RabbitMQConfig
import br.edu.uaifood.orders.domain.model.Order
import br.edu.uaifood.orders.domain.model.OrderStatus
import br.edu.uaifood.orders.domain.repository.OrderRepository
import br.edu.uaifood.orders.event.dto.OrderCreatedEvent
import br.edu.uaifood.orders.event.dto.OrderPaymentRequestedEvent
import br.edu.uaifood.orders.event.dto.OrderPaymentStatusUpdatedEvent
import br.edu.uaifood.orders.event.dto.OrderStatusChangedEvent
import br.edu.uaifood.orders.exception.OrderNotFoundException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.kafka.core.KafkaTemplate
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class OrderEventPublisherTest {

    @Mock
    private lateinit var rabbitTemplate: RabbitTemplate

    @Mock
    private lateinit var kafkaTemplate: KafkaTemplate<String, OrderPaymentRequestedEvent>

    @Mock
    private lateinit var orderRepository: OrderRepository

    @InjectMocks
    private lateinit var orderEventPublisher: OrderEventPublisher

    @InjectMocks
    private lateinit var orderPaymentRequestedPublisher: OrderPaymentRequestedPublisher

    @InjectMocks
    private lateinit var orderPaymentStatusConsumer: OrderPaymentStatusConsumer

    @Test
    fun `publishOrderCreated should send event to RabbitMQ`() {
        // Given
        val event = OrderCreatedEvent(
            orderId = UUID.randomUUID(),
            customerId = "customer123",
            restaurantId = "restaurant123",
            status = OrderStatus.CREATED,
            items = emptyList()
        )

        // When
        orderEventPublisher.publishOrderCreated(event)

        // Then
        verify(rabbitTemplate).convertAndSend(
            RabbitMQConfig.ORDER_EXCHANGE,
            RabbitMQConfig.ORDER_CREATED_ROUTING_KEY,
            event
        )
    }

    @Test
    fun `publishOrderStatusChanged should send event to RabbitMQ`() {
        // Given
        val event = OrderStatusChangedEvent(
            orderId = UUID.randomUUID(),
            oldStatus = OrderStatus.CREATED,
            newStatus = OrderStatus.PENDING_PAYMENT
        )

        // When
        orderEventPublisher.publishOrderStatusChanged(event)

        // Then
        verify(rabbitTemplate).convertAndSend(
            RabbitMQConfig.ORDER_EXCHANGE,
            RabbitMQConfig.ORDER_STATUS_UPDATED_ROUTING_KEY,
            event
        )
    }

    @Test
    fun `publish should send event to Kafka`() {
        // Given
        val order = Order(
            id = UUID.randomUUID(),
            customerId = "customer123",
            restaurantId = "restaurant123",
            items = emptyList()
        )

        // When
        orderPaymentRequestedPublisher.publish(order)

        // Then
        verify(kafkaTemplate).send(
            eq("order-payment-requested"),
            eq(order.id.toString()),
            any(OrderPaymentRequestedEvent::class.java)
        )
    }

    @Test
    fun `consume should update order status when payment is approved`() {
        // Given
        val orderId = UUID.randomUUID()
        val order = Order(
            id = orderId,
            customerId = "customer123",
            restaurantId = "restaurant123",
            items = emptyList()
        )
        val event = OrderPaymentStatusUpdatedEvent(
            orderId = orderId,
            status = "APPROVED"
        )
        org.mockito.Mockito.`when`(orderRepository.findById(orderId)).thenReturn(Optional.of(order))

        // When
        orderPaymentStatusConsumer.consume(event)

        // Then
        verify(orderRepository).save(order.copy(status = OrderStatus.PAID))
    }

    @Test
    fun `consume should update order status when payment is rejected`() {
        // Given
        val orderId = UUID.randomUUID()
        val order = Order(
            id = orderId,
            customerId = "customer123",
            restaurantId = "restaurant123",
            items = emptyList()
        )
        val event = OrderPaymentStatusUpdatedEvent(
            orderId = orderId,
            status = "REJECTED"
        )
        org.mockito.Mockito.`when`(orderRepository.findById(orderId)).thenReturn(Optional.of(order))

        // When
        orderPaymentStatusConsumer.consume(event)

        // Then
        verify(orderRepository).save(order.copy(status = OrderStatus.PAYMENT_REJECTED))
    }

    @Test
    fun `consume should update order status when payment is cancelled`() {
        // Given
        val orderId = UUID.randomUUID()
        val order = Order(
            id = orderId,
            customerId = "customer123",
            restaurantId = "restaurant123",
            items = emptyList()
        )
        val event = OrderPaymentStatusUpdatedEvent(
            orderId = orderId,
            status = "CANCELLED"
        )
        org.mockito.Mockito.`when`(orderRepository.findById(orderId)).thenReturn(Optional.of(order))

        // When
        orderPaymentStatusConsumer.consume(event)

        // Then
        verify(orderRepository).save(order.copy(status = OrderStatus.CANCELLED))
    }

    @Test
    fun `consume should throw OrderNotFoundException when order not found`() {
        // Given
        val orderId = UUID.randomUUID()
        val event = OrderPaymentStatusUpdatedEvent(
            orderId = orderId,
            status = "APPROVED"
        )
        org.mockito.Mockito.`when`(orderRepository.findById(orderId)).thenReturn(Optional.empty())

        // When/Then
        assertThrows<OrderNotFoundException> {
            orderPaymentStatusConsumer.consume(event)
        }
    }

    @Test
    fun `consume should throw IllegalArgumentException for invalid payment status`() {
        // Given
        val orderId = UUID.randomUUID()
        val order = Order(
            id = orderId,
            customerId = "customer123",
            restaurantId = "restaurant123",
            items = emptyList()
        )
        val event = OrderPaymentStatusUpdatedEvent(
            orderId = orderId,
            status = "INVALID"
        )
        org.mockito.Mockito.`when`(orderRepository.findById(orderId)).thenReturn(Optional.of(order))

        // When/Then
        assertThrows<IllegalArgumentException> {
            orderPaymentStatusConsumer.consume(event)
        }
    }
} 