package br.edu.uaifood.orders.domain

import br.edu.uaifood.orders.repository.payment.entity.PaymentEntity
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class PaymentTest {

    @Test
    fun `from should create Payment from PaymentEntity`() {
        // Given
        val id = UUID.randomUUID()
        val orderId = UUID.randomUUID()
        val paymentId = "payment123"
        val amount = 100.0
        val qrCode = "qr123"
        val paymentEntity = PaymentEntity(
            id = id,
            orderId = orderId,
            status = PaymentStatus.PENDING,
            paymentId = paymentId,
            amount = amount,
            qrCode = qrCode
        )

        // When
        val payment = Payment.from(paymentEntity)

        // Then
        assertNotNull(payment)
        assertEquals(id, payment.id)
        assertEquals(orderId, payment.orderId)
        assertEquals(PaymentStatus.PENDING, payment.status)
        assertEquals(paymentId, payment.paymentId)
        assertEquals(amount, payment.amount)
        assertEquals(qrCode, payment.qrCode)
    }

    @Test
    fun `Payment constructor should create instance with default values`() {
        // Given
        val orderId = UUID.randomUUID()
        val paymentId = null
        val qrCode = null

        // When
        val payment = Payment(
            orderId = orderId,
            status = PaymentStatus.PENDING,
            amount = 100.0,
            paymentId = paymentId,
            qrCode = qrCode
        )

        // Then
        assertNotNull(payment)
        assertNotNull(payment.id)
        assertEquals(orderId, payment.orderId)
        assertEquals(PaymentStatus.PENDING, payment.status)
        assertEquals(100.0, payment.amount)
        assertEquals(paymentId, payment.paymentId)
        assertEquals(qrCode, payment.qrCode)
    }
} 