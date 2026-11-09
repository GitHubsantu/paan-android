package com.paan.safa.domain.repository

import com.paan.safa.domain.model.PaymentStatus

data class CheckoutSession(val checkoutUrl: String?, val paymentStatus: PaymentStatus)
data class PaymentCheck(val paymentStatus: PaymentStatus, val failureReason: String?)

interface GatewayPaymentRepository {
    /** Asks our server for the gateway's hosted checkout URL. */
    suspend fun startCheckout(orderId: String): Result<CheckoutSession>
    /** Cheap status check. The server's webhook is the source of truth. */
    suspend fun checkPayment(orderId: String): Result<PaymentCheck>
}