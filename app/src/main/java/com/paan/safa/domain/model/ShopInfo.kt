package com.paan.safa.domain.model

import com.paan.safa.core.util.AppConfig

data class ShopInfo(
    val isOpen: Boolean,
    val hours: String? = null,
    val name: String = "",
    val address: String = "",
    val phone: String = "",
    val pickupMessage: String = "",
    val statusMessage: String = "",
)

val ShopInfo?.isClosed: Boolean get() = this != null && !this.isOpen

/** Server ka shop name; load hone tak BRAND_NAME dikhega. */
val ShopInfo?.displayName: String
    get() = this?.name?.takeIf { it.isNotBlank() } ?: AppConfig.BRAND_NAME

/** Server ka phone, ya null agar blank hai. */
val ShopInfo?.callNumber: String? get() = this?.phone?.takeIf { it.isNotBlank() }