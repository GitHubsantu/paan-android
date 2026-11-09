package com.paan.safa.core.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** Remembers which order a tapped notification wants to open, until the navigation is ready. */
@Singleton
class DeepLinkHolder @Inject constructor() {
    private val _orderId = MutableStateFlow<String?>(null)
    val orderId: StateFlow<String?> = _orderId.asStateFlow()

    fun open(id: String) { _orderId.value = id }
    fun consume() { _orderId.value = null }
}