package com.paan.safa.data.repository

import com.paan.safa.data.remote.PaanApi
import com.paan.safa.data.remote.apiCall
import com.paan.safa.data.remote.toPaymentStatus
import com.paan.safa.domain.repository.CheckoutSession
import com.paan.safa.domain.repository.GatewayPaymentRepository
import com.paan.safa.domain.repository.PaymentCheck
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteGatewayPaymentRepository @Inject constructor(
    private val api: PaanApi,
) : GatewayPaymentRepository {

    override suspend fun startCheckout(orderId: String): Result<CheckoutSession> =
        apiCall { api.payOrder(orderId) }.map {
            CheckoutSession(it.checkoutUrl?.takeIf { u -> u.isNotBlank() }, it.paymentStatus.toPaymentStatus())
        }

    override suspend fun checkPayment(orderId: String): Result<PaymentCheck> =
        apiCall { api.orderStatus(orderId) }.map {
            PaymentCheck(it.paymentStatus.toPaymentStatus(), it.failureReason)
        }
}