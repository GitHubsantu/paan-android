package com.paan.safa.ui.screens.home

import com.paan.safa.data.remote.asUiText
import com.paan.safa.core.ui.UiText
import com.paan.safa.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paan.safa.core.util.ticker
import com.paan.safa.domain.model.Category
import com.paan.safa.domain.model.ItemOptions
import com.paan.safa.domain.model.Order
import com.paan.safa.domain.model.Product
import com.paan.safa.domain.repository.CartRepository
import com.paan.safa.domain.repository.OrderRepository
import com.paan.safa.domain.repository.ProductRepository
import com.paan.safa.domain.repository.NotificationRepository
import com.paan.safa.domain.model.ShopInfo
import com.paan.safa.domain.repository.ShopRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val error: UiText? = null,
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: String? = null,      // null = All
    val query: String = "",
    val featured: List<Product> = emptyList(),
    val products: List<Product> = emptyList(),   // filtered
    val quantities: Map<String, Int> = emptyMap(),
    val cartCount: Int = 0,
    val cartTotal: Int = 0,
    val activeOrders: List<Order> = emptyList(), // all orders still in progress, newest first
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val cart: CartRepository,
    private val orders: OrderRepository,
    private val notifications: NotificationRepository,
    private val shops: ShopRepository,
) : ViewModel() {

    private data class Local(
        val isLoading: Boolean = true,
        val error: UiText? = null,
        val categories: List<Category> = emptyList(),
        val all: List<Product> = emptyList(),
        val selected: String? = null,
        val query: String = "",
    )

    private val local = MutableStateFlow(Local())

    // Keeps active-order cards fresh while Home is visible; skips the call when nothing is in progress.
    private val orderPolling = ticker(15_000) { orders.refreshIfNeeded() }
    // Keeps the bell badge fresh while Home is visible (every 60 s).
    private val unreadPolling = ticker(60_000) { notifications.refreshUnreadCount() }

    val unreadCount: StateFlow<Int> =
        combine(notifications.unreadCount, unreadPolling) { count, _ -> count }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), notifications.unreadCount.value)

    // Keeps the Open/Closed status fresh while Home is visible (every 60 s).
    private val shopPolling = ticker(60_000) { shops.refresh() }

    val shop: StateFlow<ShopInfo?> =
        combine(shops.shop, shopPolling) { s, _ -> s }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), shops.shop.value)

    val state: StateFlow<HomeUiState> =
        combine(local, cart.items, orders.orders, orderPolling) { l, items, orderList, _ ->
            val q = l.query.trim()
            val filtered = l.all.filter { p ->
                !p.isFeatured &&
                        (l.selected == null || p.categoryId == l.selected) &&
                        (q.isEmpty() || p.name.contains(q, true) || p.description.contains(q, true))
            }
            HomeUiState(
                isLoading = l.isLoading,
                error = l.error,
                categories = l.categories,
                selectedCategoryId = l.selected,
                query = l.query,
                featured = if (l.selected == null && q.isEmpty()) l.all.filter { it.isFeatured && it.isAvailable } else emptyList(),
                products = filtered,
                quantities = items.groupBy { it.product.id }.mapValues { (_, lines) -> lines.sumOf { it.quantity } },
                cartCount = items.sumOf { it.quantity },
                cartTotal = items.sumOf { it.lineTotal },
                activeOrders = orderList.filter { it.status.isActive },
            )
        }.flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    init { load() }

    fun load() {
        viewModelScope.launch {
            local.update { it.copy(isLoading = true, error = null) }
            val (categories, products) = coroutineScope {
                val c = async { productRepository.getCategories() }
                val p = async { productRepository.getProducts() }
                c.await() to p.await()
            }
            val failure = categories.exceptionOrNull() ?: products.exceptionOrNull()
            if (failure != null) {
                local.update { it.copy(isLoading = false, error = failure.asUiText() ?: UiText.Res(R.string.common_something_went_wrong)) }
            } else {
                local.update {
                    it.copy(isLoading = false, categories = categories.getOrThrow(), all = products.getOrThrow())
                }
            }
        }
    }

    fun onCategorySelected(id: String?) = local.update { it.copy(selected = id) }
    fun onQueryChange(q: String) = local.update { it.copy(query = q) }

    fun add(product: Product) = cart.add(product)
    fun increment(product: Product) = cart.add(product)
    fun decrement(product: Product) {
        val lines = cart.items.value.filter { it.product.id == product.id }
        val line = lines.firstOrNull { it.options == ItemOptions.Default } ?: lines.firstOrNull() ?: return
        cart.setQuantity(line.lineId, line.quantity - 1)
    }
}