package com.paan.safa.ui.screens.product

import com.paan.safa.data.remote.asUiText
import com.paan.safa.core.ui.UiText
import com.paan.safa.R
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paan.safa.core.navigation.Route
import com.paan.safa.core.util.AppConfig
import com.paan.safa.domain.model.ItemOptions
import com.paan.safa.domain.model.Product
import com.paan.safa.domain.model.SupariType
import com.paan.safa.domain.model.Sweetness
import com.paan.safa.domain.model.isClosed
import com.paan.safa.domain.model.displayName
import com.paan.safa.domain.repository.CartRepository
import com.paan.safa.domain.repository.ProductRepository
import com.paan.safa.domain.repository.ShopRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductDetailUiState(
    val isLoading: Boolean = true,
    val error: UiText? = null,
    val product: Product? = null,
    val quantity: Int = 1,
    val options: ItemOptions = ItemOptions.Default,
    val shopClosed: Boolean = false,
    val shopName: String = "",
) {
    val unitPrice: Int get() = product?.price ?: 0
    val total: Int get() = unitPrice * quantity          // options never change the price
    val canAdd: Boolean get() = product?.isAvailable == true && !shopClosed
}

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val products: ProductRepository,
    private val cart: CartRepository,
    private val shops: ShopRepository,
) : ViewModel() {

    private val productId: String = savedStateHandle[Route.ProductDetails.ARG_ID] ?: ""
    private val _state = MutableStateFlow(ProductDetailUiState())
    val state: StateFlow<ProductDetailUiState> = _state.asStateFlow()

    init {
        load()
        viewModelScope.launch {
            shops.shop.collect { s -> _state.update { it.copy(shopClosed = s.isClosed, shopName = s.displayName) } }
        }
        viewModelScope.launch { shops.refresh() }
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            products.getProduct(productId)
                .onSuccess { p -> _state.update { it.copy(isLoading = false, product = p) } }
                .onFailure { e -> _state.update { it.copy(isLoading = false, error = e.asUiText() ?: UiText.Res(R.string.error_product_not_found)) } }
        }
    }

    fun increment() = _state.update {
        it.copy(quantity = (it.quantity + 1).coerceAtMost(AppConfig.MAX_ITEM_QUANTITY))
    }

    fun decrement() = _state.update { it.copy(quantity = (it.quantity - 1).coerceAtLeast(1)) }

    fun onSweetnessSelected(value: Sweetness) = _state.update { it.copy(options = it.options.copy(sweetness = value)) }
    fun onSupariSelected(value: SupariType) = _state.update { it.copy(options = it.options.copy(supari = value)) }

    /** Adds the selected quantity and options to the cart, then lets the screen navigate. */
    fun addToCart(onDone: () -> Unit) {
        val s = _state.value
        val product = s.product ?: return
        if (!s.canAdd) return
        cart.add(product, s.quantity, s.options)
        onDone()
    }
}