package com.paan.safa.ui.screens.otp

import com.paan.safa.data.remote.asUiText
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paan.safa.core.navigation.Route
import com.paan.safa.core.util.AppConfig
import com.paan.safa.core.ui.UiText
import com.paan.safa.R
import com.paan.safa.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OtpUiState(
    val phone: String = "",
    val otp: String = "",
    val error: UiText? = null,
    val isVerifying: Boolean = false,
    val secondsLeft: Int = AppConfig.OTP_RESEND_SECONDS,
) {
    val canResend get() = secondsLeft == 0
    val canVerify get() = otp.length == AppConfig.OTP_LENGTH && !isVerifying
}

@HiltViewModel
class OtpViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val auth: AuthRepository,
) : ViewModel() {

    private val phone: String = savedStateHandle[Route.Otp.ARG_PHONE] ?: ""
    private val _state = MutableStateFlow(OtpUiState(phone = phone))
    val state: StateFlow<OtpUiState> = _state.asStateFlow()
    private var timerJob: Job? = null

    init { startTimer() }

    private fun startTimer() {
        timerJob?.cancel()
        _state.update { it.copy(secondsLeft = AppConfig.OTP_RESEND_SECONDS) }
        timerJob = viewModelScope.launch {
            while (_state.value.secondsLeft > 0) {
                delay(1000)
                _state.update { it.copy(secondsLeft = it.secondsLeft - 1) }
            }
        }
    }

    fun onOtpChange(value: String) {
        val digits = value.filter { it.isDigit() }.take(AppConfig.OTP_LENGTH)
        _state.update { it.copy(otp = digits, error = null) }
    }

    fun resend() {
        if (!_state.value.canResend) return
        viewModelScope.launch {
            auth.requestOtp(phone)
                .onSuccess {
                    _state.update { it.copy(otp = "", error = null) }
                    startTimer()
                }
                .onFailure { e -> _state.update { it.copy(error = e.asUiText()) } }
        }
    }

    fun verify(onVerified: (needsName: Boolean) -> Unit) {
        val s = _state.value
        if (!s.canVerify) return
        viewModelScope.launch {
            _state.update { it.copy(isVerifying = true, error = null) }
            auth.verifyOtp(phone, s.otp)
                .onSuccess { user -> onVerified(user.name.isBlank() || user.name.equals("Guest", ignoreCase = true)) }
                .onFailure { e ->
                    _state.update { st ->
                        st.copy(isVerifying = false, otp = "", error = e.asUiText() ?: UiText.Res(R.string.error_invalid_otp))
                    }
                }
        }
    }
}
