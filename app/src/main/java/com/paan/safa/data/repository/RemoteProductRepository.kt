package com.paan.safa.data.repository

import com.paan.safa.data.remote.CategoryDto
import com.paan.safa.data.remote.PaanApi
import com.paan.safa.data.remote.apiCall
import com.paan.safa.data.remote.apiCallWithMeta
import com.paan.safa.data.remote.toDomain
import com.paan.safa.domain.model.Category
import com.paan.safa.domain.model.Product
import com.paan.safa.domain.repository.ProductRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteProductRepository @Inject constructor(
    private val api: PaanApi,
) : ProductRepository {

    // The list already carries every field, so opening a product is instant.
    @Volatile private var cache: Map<String, Product> = emptyMap()

    override suspend fun getCategories(): Result<List<Category>> =
        apiCall { api.categories() }.map { list -> list.map(CategoryDto::toDomain) }

    override suspend fun getProducts(): Result<List<Product>> {
        val all = mutableListOf<Product>()
        var page = 1
        while (true) {
            val (items, meta) = apiCallWithMeta { api.products(perPage = PAGE_SIZE, page = page) }
                .getOrElse { return Result.failure(it) }
            all += items.map { it.toDomain() }
            if (page >= (meta?.lastPage ?: 1) || page >= MAX_PAGES) break
            page++
        }
        cache = all.associateBy { it.id }
        return Result.success(all)
    }

    override suspend fun getProduct(id: String): Result<Product> =
        cache[id]?.let { Result.success(it) }
            ?: apiCall { api.product(id) }.map { it.toDomain() }

    private companion object {
        const val PAGE_SIZE = 50
        const val MAX_PAGES = 20
    }
}