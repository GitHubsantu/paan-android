package com.paan.safa.ui.screens.order

import com.paan.safa.data.remote.asUiText
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.paan.safa.R
import com.paan.safa.core.ui.UiText
import androidx.lifecycle.viewModelScope
import com.paan.safa.core.navigation.Route
import com.paan.safa.core.util.parsePickupQr
import com.paan.safa.core.util.ticker
import com.paan.safa.data.remote.ApiException
import com.paan.safa.domain.model.Order
import com.paan.safa.domain.repository.AuthRepository
import com.paan.safa.domain.repository.OrderRepository
import com.paan.safa.domain.model.ShopInfo
import com.paan.safa.domain.repository.ShopRepository
import kotlinx.coroutines.flow.Flow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OrderStatusUiState(
    val isLoading: Boolean = true,
    val order: Order? = null,
    val customerPhone: String = "",
    val shop: ShopInfo? = null,
    val error: UiText? = null,
    val pickupBusy: Boolean = false,
    val message: UiText? = null,   // one-time snackbar text
)

@HiltViewModel
class OrderStatusViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val orders: OrderRepository,
    auth: AuthRepository,
    private val shops: ShopRepository,
) : ViewModel() {

    private data class Load(val done: Boolean = false, val error: UiText? = null)
    private data class Pickup(val busy: Boolean = false, val message: UiText? = null)

    private val orderId: String = savedStateHandle[Route.OrderStatus.ARG_ID] ?: ""
    private val load = MutableStateFlow(Load())
    private val pickup = MutableStateFlow(Pickup())

    // Checks the shop's progress every 5 s while the screen is visible, and stops once the order is finished.
    private val polling = ticker(5_000) {
        val status = orders.observeOrder(orderId).first()?.status
        if (status == null || status.isActive) {
            val result = orders.refreshOrder(orderId)
            load.value = Load(done = true, error = result.exceptionOrNull()?.asUiText())
        } else {
            load.value = Load(done = true)
        }
    }

    private val base: Flow<OrderStatusUiState> =
        combine(orders.observeOrder(orderId), auth.session, load, polling, pickup) { order, user, l, _, p ->
            OrderStatusUiState(
                isLoading = order == null && !l.done,
                order = order,
                customerPhone = user?.phone.orEmpty(),
                error = if (order == null) l.error else null,
                pickupBusy = p.busy,
                message = p.message,
            )
        }

    val state: StateFlow<OrderStatusUiState> =
        combine(base, shops.shop) { s, shop -> s.copy(shop = shop) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OrderStatusUiState())

    init { viewModelScope.launch { shops.refresh() } }

    /** The scanner returned some text. The server decides if it is valid. */
    fun onScanned(raw: String) {
        val order = state.value.order ?: return
        if (pickup.value.busy) return
        val qr = parsePickupQr(raw)
        when {
            qr == null -> show(UiText.Res(R.string.pickup_wrong_qr))
            qr.orderNumber != order.orderNumber ->
                show(UiText.Res(R.string.pickup_different_order, listOf(order.orderNumber)))
            else -> viewModelScope.launch {
                pickup.update { it.copy(busy = true, message = null) }
                orders.pickupOrder(order.id, qr.code)
                    .onSuccess { pickup.update { it.copy(busy = false, message = UiText.Res(R.string.pickup_success)) } }
                    .onFailure { e ->
                        pickup.update { it.copy(busy = false, message = pickupError(e)) }
                        val code = (e as? ApiException)?.errorCode
                        if (code == "ALREADY_PICKED_UP" || code == "ORDER_NOT_READY") orders.refreshOrder(order.id)
                    }
            }
        }
    }

    fun showMessage(text: UiText) = show(text)

    fun messageShown() = pickup.update { it.copy(message = null) }

    private fun show(text: UiText) = pickup.update { it.copy(message = text) }

    private fun pickupError(e: Throwable): UiText = when ((e as? ApiException)?.errorCode) {
        "ALREADY_PICKED_UP" -> UiText.Res(R.string.pickup_already_done)
        "ORDER_NOT_READY" -> UiText.Res(R.string.pickup_not_ready)
        "PICKUP_CODE_INVALID" -> UiText.Res(R.string.pickup_invalid_code)
        "TOO_MANY_ATTEMPTS" -> UiText.Res(R.string.pickup_too_many_attempts)
        else -> e.asUiText() ?: UiText.Res(R.string.common_something_went_wrong)
    }
}
