package com.paan.safa.domain.repository

import com.paan.safa.domain.model.CartItem
import com.paan.safa.domain.model.Order
import com.paan.safa.domain.model.PaymentMethod
import com.paan.safa.domain.model.PaymentStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface OrderRepository {
    val orders: StateFlow<List<Order>>
    fun observeOrder(id: String): Flow<Order?>

    suspend fun createOrder(
        items: List<CartItem>,
        comment: String,
        paymentMethod: PaymentMethod,
        paymentStatus: PaymentStatus,   // the server decides the real payment status
    ): Result<Order>

    /** Reloads the order list from the server. */
    suspend fun refresh(): Result<Unit>
    /** Reloads only when never loaded, or when some order is still in progress. */
    suspend fun refreshIfNeeded(): Result<Unit>
    /** Updates one order (cheap status call if it's already known). */
    suspend fun refreshOrder(id: String): Result<Unit>
    /** Completes a ready order with the code from the shop's QR. The server checks everything. */
    suspend fun pickupOrder(id: String, code: String): Result<Order>
    /** Forgets cached orders (on logout). */
    fun clearLocal()
}