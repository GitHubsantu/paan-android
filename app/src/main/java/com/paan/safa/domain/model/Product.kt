package com.paan.safa.domain.model

data class Category(val id: String, val name: String)

data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Int,                 // whole rupees
    val imageUrl: String?,
    val categoryId: String,
    val isAvailable: Boolean = true,
    val rating: Double = 4.5,
    val tag: String? = null,        // e.g. "Chandi Vark"
    val prepMinutes: Int = 3,
    val isFeatured: Boolean = false,
    val ingredients: List<String> = emptyList(),
    val thumbnailUrl: String? = null,
) {
    /** Small image for lists and the cart; falls back to the full image. */
    val listImageUrl: String? get() = thumbnailUrl ?: imageUrl
}