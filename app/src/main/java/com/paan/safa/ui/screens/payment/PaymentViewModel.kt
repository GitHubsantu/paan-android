package com.paan.safa.ui.screens.payment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paan.safa.R
import com.paan.safa.core.navigation.Route
import com.paan.safa.core.ui.UiText
import com.paan.safa.data.remote.asUiText
import com.paan.safa.domain.model.PaymentStatus
import com.paan.safa.domain.repository.GatewayPaymentRepository
import com.paan.safa.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface PaymentUiState {
    data object Idle : PaymentUiState
    data object Starting : PaymentUiState      // asking our server for the checkout URL
    data object InGateway : PaymentUiState     // Custom Tab is open, user is paying
    data object Confirming : PaymentUiState    // polling our server for the result
    data class Paid(val orderId: String) : PaymentUiState
    data class Failed(val message: UiText) : PaymentUiState
    data object Timeout : PaymentUiState       // no confirmation yet (webhook may be late)
}

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val gateway: GatewayPaymentRepository,
    private val orders: OrderRepository,
    private val savedState: SavedStateHandle,
) : ViewModel() {

    private val orderId: String = savedState[Route.Payment.ARG_ID] ?: ""

    // Survives process death while the user is in the browser / UPI app.
    private val _state = MutableStateFlow<PaymentUiState>(
        if (savedState.get<Boolean>(KEY_LAUNCHED) == true) PaymentUiState.InGateway else PaymentUiState.Idle,
    )
    val uiState: StateFlow<PaymentUiState> = _state.asStateFlow()

    private val _openCheckout = Channel<String>(Channel.BUFFERED)
    val openCheckout = _openCheckout.receiveAsFlow()

    private var pollJob: Job? = null

    fun start() { if (_state.value is PaymentUiState.Idle) launchPayment() }

    /** "Try again" / "Reopen" / "Pay again". The server reuses a still-valid pending checkout. */
    fun retry() = launchPayment()

    /** Screen resumed: if the user was in the gateway they're back, so confirm with the server. */
    fun onResume() { if (_state.value is PaymentUiState.InGateway) poll() }

    fun checkAgain() = poll()

    fun stopWaiting() {
        pollJob?.cancel()
        _state.value = PaymentUiState.Timeout
    }

    private fun launchPayment() {
        pollJob?.cancel()
        _state.value = PaymentUiState.Starting
        viewModelScope.launch {
            gateway.startCheckout(orderId)
                .onSuccess { session ->
                    val url = session.checkoutUrl
                    when {
                        session.paymentStatus == PaymentStatus.PAID -> markPaid()
                        url == null -> finish(PaymentUiState.Failed(UiText.Res(R.string.payment_open_failed)))
                        else -> {
                            savedState[KEY_LAUNCHED] = true
                            _state.value = PaymentUiState.InGateway
                            _openCheckout.send(url)
                        }
                    }
                }
                .onFailure { e ->
                    finish(PaymentUiState.Failed(e.asUiText() ?: UiText.Res(R.string.error_payment_failed)))
                }
        }
    }

    private fun poll() {
        if (pollJob?.isActive == true) return
        pollJob = viewModelScope.launch {
            _state.value = PaymentUiState.Confirming
            repeat(MAX_POLLS) {
                val check = gateway.checkPayment(orderId).getOrNull()
                when (check?.paymentStatus) {
                    PaymentStatus.PAID -> { markPaid(); return@launch }
                    PaymentStatus.FAILED -> {
                        val reason = check?.failureReason?.takeIf { it.isNotBlank() }
                        finish(PaymentUiState.Failed(reason?.let { UiText.Plain(it) } ?: UiText.Res(R.string.error_payment_failed)))
                        return@launch
                    }
                    else -> Unit
                }
                delay(POLL_INTERVAL_MS)   // network errors just retry on the next tick
            }
            _state.value = PaymentUiState.Timeout
        }
    }

    private suspend fun markPaid() {
        orders.refreshOrder(orderId)   // so the confirmation screen shows "Paid"
        finish(PaymentUiState.Paid(orderId))
    }

    private fun finish(terminal: PaymentUiState) {
        savedState[KEY_LAUNCHED] = false
        _state.value = terminal
    }

    private companion object {
        const val KEY_LAUNCHED = "launched"
        const val MAX_POLLS = 20            // 20 x 2 s = about 40 s
        const val POLL_INTERVAL_MS = 2_000L
    }
}