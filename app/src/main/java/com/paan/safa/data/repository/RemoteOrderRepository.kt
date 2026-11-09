package com.paan.safa.data.repository

import com.paan.safa.R
import com.paan.safa.data.remote.ApiException
import com.paan.safa.data.remote.CreateOrderRequest
import com.paan.safa.data.remote.PaanApi
import com.paan.safa.data.remote.apiCall
import com.paan.safa.data.remote.channel
import com.paan.safa.data.remote.toDomain
import com.paan.safa.data.remote.toOrderStatus
import com.paan.safa.data.remote.toPaymentStatus
import com.paan.safa.data.remote.toRequest
import com.paan.safa.domain.model.CartItem
import com.paan.safa.domain.model.Order
import com.paan.safa.domain.model.PaymentMethod
import com.paan.safa.domain.model.PaymentStatus
import com.paan.safa.domain.repository.OrderRepository
import com.paan.safa.data.remote.PickupRequest
import com.paan.safa.data.remote.resolveOrderStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteOrderRepository @Inject constructor(
    private val api: PaanApi,
) : OrderRepository {

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    override val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    @Volatile private var loadedOnce = false

    override fun observeOrder(id: String): Flow<Order?> =
        _orders.map { list -> list.firstOrNull { it.id == id } }.distinctUntilChanged()

    override suspend fun createOrder(
        items: List<CartItem>,
        comment: String,
        paymentMethod: PaymentMethod,
        paymentStatus: PaymentStatus,
    ): Result<Order> {
        if (items.isEmpty()) return Result.failure(ApiException("Your tray is empty.", messageRes = R.string.error_tray_empty))
        val body = CreateOrderRequest(
            items = items.map { it.toRequest() },
            comment = comment.trim().ifEmpty { null },
            paymentMethod = "gateway",
            paymentChannel = paymentMethod.channel,
        )
        // The server calculates the real totals and payment status; the key prevents duplicate orders on retries.
        return apiCall { api.createOrder(UUID.randomUUID().toString(), body) }
            .map { dto -> dto.toDomain().also(::upsert) }
    }

    override suspend fun refresh(): Result<Unit> =
        apiCall { api.orders(perPage = PAGE_SIZE, page = 1) }.map { list ->
            _orders.value = list.map { it.toDomain() }.sortedByDescending { it.createdAt }
            loadedOnce = true
            Unit
        }

    override suspend fun refreshIfNeeded(): Result<Unit> =
        if (!loadedOnce || _orders.value.any { it.status.isActive }) refresh() else Result.success(Unit)

    override suspend fun refreshOrder(id: String): Result<Unit> {
        val cached = _orders.value.firstOrNull { it.id == id }
        return if (cached == null) {
            apiCall { api.order(id) }.map { dto -> upsert(dto.toDomain()) }
        } else {
            apiCall { api.orderStatus(id) }.map { s ->
                upsert(
                    cached.copy(
                        status = resolveOrderStatus(s.status, s.paymentStatus, cached.createdAt),
                        paymentStatus = s.paymentStatus.toPaymentStatus(),
                    )
                )
            }
        }
    }

    override suspend fun pickupOrder(id: String, code: String): Result<Order> =
        apiCall { api.pickupOrder(id, PickupRequest(code)) }
            .map { dto -> dto.toDomain().also(::upsert) }

    override fun clearLocal() {
        _orders.value = emptyList()
        loadedOnce = false
    }

    private fun upsert(order: Order) {
        _orders.update { list ->
            (listOf(order) + list.filterNot { it.id == order.id }).sortedByDescending { it.createdAt }
        }
    }

    private companion object {
        const val PAGE_SIZE = 50
    }
}