package br.edu.uaifood.orders.domain

import br.edu.uaifood.orders.domain.model.Order
import br.edu.uaifood.orders.repository.payment.entity.PaymentEntity
import java.util.UUID

data class Payment(
    val id: UUID = UUID.randomUUID(),
    val orderId: UUID,
    val status: PaymentStatus,
    val paymentId: String?,
    val amount: Double,
    val qrCode: String?
) {
    companion object {
        fun from(paymentPersisted: PaymentEntity): Payment =
            Payment(
                id = paymentPersisted.id,
                orderId = paymentPersisted.orderId,
                status = paymentPersisted.status,
                paymentId = paymentPersisted.paymentId,
                amount = paymentPersisted.amount,
                qrCode = paymentPersisted.qrCode
            )
    }
}

enum class PaymentStatus {
    PENDING,
    APPROVED,
    DECLINED,
    CANCELLED
}