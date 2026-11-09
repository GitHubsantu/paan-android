package com.paan.safa.ui.screens.checkout

import com.paan.safa.data.remote.asUiText
import androidx.lifecycle.ViewModel
import com.paan.safa.R
import com.paan.safa.core.ui.UiText
import androidx.lifecycle.viewModelScope
import com.paan.safa.data.remote.ApiException
import com.paan.safa.domain.model.CartItem
import com.paan.safa.domain.model.PaymentMethod
import com.paan.safa.domain.model.PaymentStatus
import com.paan.safa.domain.model.isClosed
import com.paan.safa.domain.repository.CartRepository
import com.paan.safa.domain.repository.OrderRepository
import com.paan.safa.domain.repository.ShopRepository
import com.paan.safa.domain.model.displayName
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CheckoutUiState(
    val items: List<CartItem> = emptyList(),
    val comment: String = "",
    val method: PaymentMethod = PaymentMethod.Upi,
    val isProcessing: Boolean = false,
    val error: UiText? = null,
    val shopClosed: Boolean = false,
    val shopName: String = "",
) {
    val isEmpty: Boolean get() = items.isEmpty()
    val total: Int get() = items.sumOf { it.lineTotal }
    val itemCount: Int get() = items.sumOf { it.quantity }
}

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val cart: CartRepository,
    private val orders: OrderRepository,
    private val shops: ShopRepository,
) : ViewModel() {

    private data class Local(
        val method: PaymentMethod = PaymentMethod.Upi,
        val isProcessing: Boolean = false,
        val error: UiText? = null,
    )

    private val local = MutableStateFlow(Local())

    val state: StateFlow<CheckoutUiState> =
        combine(cart.items, cart.comment, local, shops.shop) { items, comment, l, shop ->
            CheckoutUiState(items, comment, l.method, l.isProcessing, l.error, shop.isClosed, shop.displayName)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CheckoutUiState())

    init { viewModelScope.launch { shops.refresh() } }

    fun onMethodSelected(method: PaymentMethod) =
        local.update { if (it.isProcessing) it else it.copy(method = method, error = null) }

    /** Creates the (unpaid) order, then the Payment screen takes the money through the gateway. */
    fun pay(onPaid: (orderId: String) -> Unit) {
        val s = state.value
        if (s.isEmpty || s.isProcessing) return
        viewModelScope.launch {
            local.update { it.copy(isProcessing = true, error = null) }

            // Ask the server first: never take payment when the shop is closed.
            val shopNow = shops.refresh().getOrNull()
            if (shopNow != null && !shopNow.isOpen) {
                local.update { it.copy(isProcessing = false, error = UiText.Res(R.string.error_shop_closed)) }
                return@launch
            }

            placeOrder(s, PaymentStatus.PENDING, onPaid)
        }
    }

    private suspend fun placeOrder(s: CheckoutUiState, status: PaymentStatus, onDone: (orderId: String) -> Unit) {
        orders.createOrder(s.items, s.comment, s.method, status)
            .onSuccess { order ->
                cart.clear()
                // isProcessing stays true so the screen never flashes "empty" before navigating.
                onDone(order.id)
            }
            .onFailure { e ->
                val closed = (e as? ApiException)?.errorCode == "SHOP_CLOSED"
                if (closed) shops.refresh()
                local.update { l ->
                    l.copy(
                        isProcessing = false,
                        error = if (closed) UiText.Res(R.string.error_shop_closed)
                        else e.asUiText() ?: UiText.Res(R.string.error_place_order),
                    )
                }
            }
    }
}
