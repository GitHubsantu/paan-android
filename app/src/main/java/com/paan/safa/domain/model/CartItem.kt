package com.paan.safa.domain.model

data class CartItem(
    val product: Product,
    val quantity: Int,
    val options: ItemOptions = ItemOptions.Default,
) {
    /** Same paan with different options = different cart lines. */
    val lineId: String
        get() = "${product.id}|${options.sweetness.name}|${options.supari.name}"

    val lineTotal: Int get() = product.price * quantity
}