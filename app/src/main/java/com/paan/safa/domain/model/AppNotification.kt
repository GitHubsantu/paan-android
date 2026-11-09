package com.paan.safa.domain.model

data class AppNotification(
    val id: String,
    val title: String,
    val body: String,
    val orderId: String?,
    val status: OrderStatus?,
    val readAt: Long?,
    val createdAt: Long,
) {
    val isRead: Boolean get() = readAt != null
}