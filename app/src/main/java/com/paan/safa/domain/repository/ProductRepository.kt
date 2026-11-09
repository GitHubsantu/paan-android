package com.paan.safa.domain.repository

import com.paan.safa.domain.model.Category
import com.paan.safa.domain.model.Product

interface ProductRepository {
    suspend fun getCategories(): Result<List<Category>>
    suspend fun getProducts(): Result<List<Product>>
    suspend fun getProduct(id: String): Result<Product>
}