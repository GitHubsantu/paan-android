package com.paan.safa.core.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paan.safa.domain.repository.AuthRepository
import com.paan.safa.domain.repository.CartRepository
import com.paan.safa.domain.repository.NotificationRepository
import com.paan.safa.domain.repository.OrderRepository
import com.paan.safa.push.DeviceTokenRepository
import com.paan.safa.core.localization.LanguageStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    auth: AuthRepository,
    cart: CartRepository,
    orders: OrderRepository,
    private val notifications: NotificationRepository,
    private val deviceTokens: DeviceTokenRepository,
    private val deepLinks: DeepLinkHolder,
    private val languageStore: LanguageStore,
) : ViewModel() {

    val hasChosenLanguage: Boolean get() = languageStore.hasChosen

    val loggedIn: StateFlow<Boolean?> = auth.session
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** The order a tapped notification wants to open (null when none). */
    val pendingOrderId: StateFlow<String?> = deepLinks.orderId
    fun consumeDeepLink() = deepLinks.consume()

    /** The user tapped a push for this order: its notifications are now read. */
    fun markOrderNotificationsRead(orderId: String) {
        viewModelScope.launch { notifications.markOrderRead(orderId) }
    }

    /** Called every time the app comes to the foreground. */
    fun onAppResumed() {
        if (loggedIn.value == true) viewModelScope.launch { notifications.refreshUnreadCount() }
    }

    init {
        viewModelScope.launch {
            auth.session.map { it != null }.distinctUntilChanged().collect { signedIn ->
                if (!signedIn) {
                    cart.clear()
                    orders.clearLocal()
                    notifications.clearLocal()
                    auth.refreshProfile()
                }
            }
        }
        viewModelScope.launch {
            var wasSignedIn = false
            auth.session.map { it != null }.distinctUntilChanged().collect { signedIn ->
                if (signedIn) {
                    wasSignedIn = true
                    deviceTokens.register()                // after login and at every app start
                    notifications.refreshUnreadCount()     // badge after login / app start
                } else if (wasSignedIn) {
                    wasSignedIn = false
                    deviceTokens.deleteLocalToken()        // logout or expired login
                }
            }
        }
    }
}
