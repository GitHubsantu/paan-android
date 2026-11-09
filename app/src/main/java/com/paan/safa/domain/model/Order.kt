package com.paan.safa.domain.model

import androidx.annotation.StringRes
import com.paan.safa.R

enum class OrderStatus(@StringRes val labelRes: Int) {
    PENDING(R.string.order_status_received),
    MAKING(R.string.order_status_preparing),
    READY(R.string.order_status_ready),
    COMPLETED(R.string.order_status_completed),
    ADMIN_CANCELLED(R.string.order_status_cancelled_by_shop),
    PAYMENT_FAILED(R.string.order_status_payment_failed);

    /** Orders still in progress at the shop. */
    val isActive: Boolean get() = this == PENDING || this == MAKING || this == READY
}

enum class PaymentStatus(@StringRes val labelRes: Int) {
    PAID(R.string.payment_status_paid),
    PENDING(R.string.payment_status_pending),
    FAILED(R.string.payment_status_failed),
    REFUNDED(R.string.payment_status_refunded),
}

data class OrderItem(
    val productId: String,
    val name: String,
    val unitPrice: Int,
    val quantity: Int,
    val options: ItemOptions,
    val imageUrl: String?,
) {
    val lineTotal: Int get() = unitPrice * quantity
}

data class Order(
    val id: String,
    val orderNumber: String,       // e.g. PAAN-10001
    val token: Int,                // shown at the counter
    val items: List<OrderItem>,
    val total: Int,
    val comment: String,
    val paymentMethod: PaymentMethod,
    val paymentStatus: PaymentStatus,
    val status: OrderStatus,
    val createdAt: Long,
) {
    val itemCount: Int get() = items.sumOf { it.quantity }
}
