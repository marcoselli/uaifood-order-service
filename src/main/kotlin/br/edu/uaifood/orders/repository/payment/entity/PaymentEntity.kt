package br.edu.uaifood.orders.repository.payment.entity

import br.edu.uaifood.orders.domain.Payment
import br.edu.uaifood.orders.domain.PaymentStatus
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "payments")
data class PaymentEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val orderId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: PaymentStatus,

    @Column
    val paymentId: String? = null,

    @Column(nullable = false)
    val amount: Double,

    @Column
    val qrCode: String? = null
) {
    companion object {
        fun from(payment: Payment): PaymentEntity =
            PaymentEntity(
                id = payment.id,
                orderId = payment.orderId,
                status = payment.status,
                paymentId = payment.paymentId,
                amount = payment.amount,
                qrCode = payment.qrCode
            )
    }

    fun toDomain(): Payment =
        Payment(
            id = id,
            orderId = orderId,
            status = status,
            paymentId = paymentId,
            amount = amount,
            qrCode = qrCode
        )
}