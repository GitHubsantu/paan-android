package com.paan.safa.domain.repository

import com.paan.safa.domain.model.ShopInfo
import kotlinx.coroutines.flow.StateFlow

interface ShopRepository {
    /** null until the first successful load. */
    val shop: StateFlow<ShopInfo?>
    suspend fun refresh(): Result<ShopInfo>
}