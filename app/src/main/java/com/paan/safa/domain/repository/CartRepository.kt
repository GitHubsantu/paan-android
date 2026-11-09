package com.paan.safa.domain.repository

import com.paan.safa.domain.model.CartItem
import com.paan.safa.domain.model.ItemOptions
import com.paan.safa.domain.model.Product
import kotlinx.coroutines.flow.StateFlow

interface CartRepository {
    val items: StateFlow<List<CartItem>>
    /** Optional note for the whole order (max AppConfig.MAX_COMMENT_LENGTH characters). */
    val comment: StateFlow<String>

    fun add(product: Product, quantity: Int = 1, options: ItemOptions = ItemOptions.Default)
    /** [lineId] comes from CartItem.lineId. quantity <= 0 removes the line. */
    fun setQuantity(lineId: String, quantity: Int)
    fun remove(lineId: String)
    fun setComment(text: String)
    /** Empties the items and the comment. */
    fun clear()
}