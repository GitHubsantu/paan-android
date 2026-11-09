package com.paan.safa.ui.screens.order

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paan.safa.core.navigation.Route
import com.paan.safa.domain.model.Order
import com.paan.safa.domain.repository.OrderRepository
import com.paan.safa.domain.model.ShopInfo
import com.paan.safa.domain.repository.ShopRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class OrderConfirmationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    orders: OrderRepository,
    shops: ShopRepository,
) : ViewModel() {
    private val orderId: String = savedStateHandle[Route.OrderConfirmation.ARG_ID] ?: ""

    val order: StateFlow<Order?> = orders.observeOrder(orderId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val shop: StateFlow<ShopInfo?> = shops.shop
}