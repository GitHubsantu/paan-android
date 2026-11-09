package com.paan.safa.data.repository

import com.paan.safa.core.util.AppConfig
import com.paan.safa.domain.model.CartItem
import com.paan.safa.domain.model.ItemOptions
import com.paan.safa.domain.model.Product
import com.paan.safa.domain.repository.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemoryCartRepository @Inject constructor() : CartRepository {
    private val _items = MutableStateFlow<List<CartItem>>(emptyList())
    override val items: StateFlow<List<CartItem>> = _items.asStateFlow()

    private val _comment = MutableStateFlow("")
    override val comment: StateFlow<String> = _comment.asStateFlow()

    override fun add(product: Product, quantity: Int, options: ItemOptions) {
        if (!product.isAvailable || quantity <= 0) return
        val incoming = CartItem(product, quantity, options)
        _items.update { list ->
            val existing = list.find { it.lineId == incoming.lineId }
            if (existing == null) {
                list + incoming.copy(quantity = quantity.coerceAtMost(AppConfig.MAX_ITEM_QUANTITY))
            } else {
                list.map {
                    if (it.lineId == incoming.lineId) {
                        it.copy(quantity = (it.quantity + quantity).coerceAtMost(AppConfig.MAX_ITEM_QUANTITY))
                    } else it
                }
            }
        }
    }

    override fun setQuantity(lineId: String, quantity: Int) {
        _items.update { list ->
            if (quantity <= 0) list.filterNot { it.lineId == lineId }
            else list.map {
                if (it.lineId == lineId) it.copy(quantity = quantity.coerceAtMost(AppConfig.MAX_ITEM_QUANTITY)) else it
            }
        }
    }

    override fun remove(lineId: String) = setQuantity(lineId, 0)

    override fun setComment(text: String) {
        _comment.value = text.take(AppConfig.MAX_COMMENT_LENGTH)
    }

    override fun clear() {
        _items.value = emptyList()
        _comment.value = ""
    }
}