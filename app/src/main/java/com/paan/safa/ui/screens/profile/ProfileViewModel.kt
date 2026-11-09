package com.paan.safa.ui.screens.profile

import com.paan.safa.data.remote.asUiText
import androidx.lifecycle.ViewModel
import com.paan.safa.R
import com.paan.safa.core.ui.UiText
import androidx.lifecycle.viewModelScope
import com.paan.safa.domain.model.OrderStatus
import com.paan.safa.domain.model.User
import com.paan.safa.domain.repository.AuthRepository
import com.paan.safa.domain.repository.CartRepository
import com.paan.safa.domain.repository.NotificationRepository
import com.paan.safa.domain.repository.OrderRepository
import com.paan.safa.domain.model.ShopInfo
import com.paan.safa.domain.repository.ShopRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: User? = null,
    val activeCount: Int = 0,
    val collectedCount: Int = 0,
    val paansOrdered: Int = 0,
    val cartCount: Int = 0,
    val unreadCount: Int = 0,
    val shop: ShopInfo? = null,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val auth: AuthRepository,
    cart: CartRepository,
    orders: OrderRepository,
    notifications: NotificationRepository,
    private val shops: ShopRepository,
) : ViewModel() {

    val state: StateFlow<ProfileUiState> =
        combine(auth.session, orders.orders, cart.items, notifications.unreadCount, shops.shop) { user, list, items, unread, shop ->
            ProfileUiState(
                user = user,
                activeCount = list.count { it.status.isActive },
                collectedCount = list.count { it.status == OrderStatus.COMPLETED },
                paansOrdered = list
                    .filter { it.status != OrderStatus.ADMIN_CANCELLED && it.status != OrderStatus.PAYMENT_FAILED }
                    .sumOf { it.itemCount },
                cartCount = items.sumOf { it.quantity },
                unreadCount = unread,
                shop = shop,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    init { viewModelScope.launch { shops.refresh() } }

    /** Returns an error message, or null when the name is acceptable. */
    fun validateName(name: String): UiText? {
        val n = name.trim()
        return when {
            n.length < 2 -> UiText.Res(R.string.error_name_short)
            n.length > MAX_NAME_LENGTH -> UiText.Res(R.string.error_name_long)
            else -> null
        }
    }

    /** Saves on the server. [onResult] gets null on success or an error message. */
    fun saveName(name: String, onResult: (UiText?) -> Unit) {
        validateName(name)?.let { onResult(it); return }
        viewModelScope.launch {
            onResult(auth.updateName(name.trim()).exceptionOrNull()?.asUiText())
        }
    }

    /** Signing out also clears the cart and cached orders (SessionViewModel watches the session). */
    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            auth.logout()
            onDone()
        }
    }

    companion object {
        const val MAX_NAME_LENGTH = 40
    }
}