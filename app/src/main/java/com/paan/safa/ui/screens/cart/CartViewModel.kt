package com.paan.safa.ui.screens.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paan.safa.core.util.AppConfig
import com.paan.safa.domain.model.CartItem
import com.paan.safa.domain.model.isClosed
import com.paan.safa.domain.repository.CartRepository
import com.paan.safa.domain.repository.ShopRepository
import com.paan.safa.domain.model.ShopInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CartUiState(
    val items: List<CartItem> = emptyList(),
    val comment: String = "",
    val shopClosed: Boolean = false,
    val shop: ShopInfo? = null,
) {
    val isEmpty: Boolean get() = items.isEmpty()
    val itemCount: Int get() = items.sumOf { it.quantity }
    val typeCount: Int get() = items.map { it.product.id }.distinct().size
    /** Total = SUM(price × quantity). No delivery, no extra charges. */
    val total: Int get() = items.sumOf { it.lineTotal }
}

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cart: CartRepository,
    private val shops: ShopRepository,
) : ViewModel() {

    val state: StateFlow<CartUiState> = combine(cart.items, cart.comment, shops.shop) { items, comment, shop ->
        CartUiState(items, comment, shop.isClosed, shop)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CartUiState())

    init { viewModelScope.launch { shops.refresh() } }

    fun increment(item: CartItem) = cart.setQuantity(item.lineId, item.quantity + 1)
    fun decrement(item: CartItem) = cart.setQuantity(item.lineId, item.quantity - 1)  // 0 removes the line
    fun remove(item: CartItem) = cart.remove(item.lineId)

    fun onCommentChange(text: String) = cart.setComment(text)

    /** Appends a quick note like "Less gulkand" unless it is already there or would exceed the limit. */
    fun addQuickNote(note: String) {
        val current = cart.comment.value
        if (current.contains(note, ignoreCase = true)) return
        val next = if (current.isBlank()) note else "$current, $note"
        if (next.length <= AppConfig.MAX_COMMENT_LENGTH) cart.setComment(next)
    }
}