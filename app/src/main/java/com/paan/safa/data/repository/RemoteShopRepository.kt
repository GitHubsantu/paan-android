package com.paan.safa.data.repository

import com.paan.safa.data.remote.PaanApi
import com.paan.safa.data.remote.apiCall
import com.paan.safa.data.remote.toDomain
import com.paan.safa.domain.model.ShopInfo
import com.paan.safa.domain.repository.ShopRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteShopRepository @Inject constructor(
    private val api: PaanApi,
) : ShopRepository {

    private val _shop = MutableStateFlow<ShopInfo?>(null)
    override val shop: StateFlow<ShopInfo?> = _shop.asStateFlow()

    // If the call fails (offline), the last known status stays.
    override suspend fun refresh(): Result<ShopInfo> =
        apiCall { api.shop() }.map { dto -> dto.toDomain().also { _shop.value = it } }
}