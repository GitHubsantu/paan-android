package com.paan.safa.ui.screens.orders

import com.paan.safa.data.remote.asUiText
import com.paan.safa.core.ui.UiText
import com.paan.safa.R
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paan.safa.core.util.ticker
import com.paan.safa.domain.model.Order
import com.paan.safa.domain.repository.CartRepository
import com.paan.safa.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class OrdersTab(@StringRes val titleRes: Int) { Active(R.string.orders_tab_active), Past(R.string.orders_tab_past) }

data class OrdersUiState(
    val tab: OrdersTab = OrdersTab.Active,
    val active: List<Order> = emptyList(),
    val past: List<Order> = emptyList(),
    val cartCount: Int = 0,
    val isLoading: Boolean = true,
    val error: UiText? = null,
) {
    val shown: List<Order> get() = if (tab == OrdersTab.Active) active else past
}

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val orders: OrderRepository,
    cart: CartRepository,
) : ViewModel() {

    private data class Load(val done: Boolean = false, val error: UiText? = null)

    private val selectedTab = MutableStateFlow(OrdersTab.Active)
    private val load = MutableStateFlow(Load())

    private val polling = ticker(15_000) {
        val result = orders.refreshIfNeeded()
        load.value = Load(done = true, error = result.exceptionOrNull()?.asUiText())
    }

    val state: StateFlow<OrdersUiState> =
        combine(selectedTab, orders.orders, cart.items, load, polling) { tab, list, items, l, _ ->
            OrdersUiState(
                tab = tab,
                active = list.filter { it.status.isActive },
                past = list.filterNot { it.status.isActive },
                cartCount = items.sumOf { it.quantity },
                isLoading = !l.done && list.isEmpty(),
                error = if (list.isEmpty()) l.error else null,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OrdersUiState())

    fun onTabSelected(tab: OrdersTab) { selectedTab.value = tab }

    fun retry() {
        viewModelScope.launch {
            load.value = Load()
            val result = orders.refresh()
            load.value = Load(done = true, error = result.exceptionOrNull()?.asUiText())
        }
    }
}