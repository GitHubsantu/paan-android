package com.paan.safa.ui.screens.login

import com.paan.safa.data.remote.asUiText
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paan.safa.core.util.AppConfig
import com.paan.safa.core.util.PhoneValidator
import com.paan.safa.core.ui.UiText
import com.paan.safa.R
import com.paan.safa.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val phone: String = "",
    val error: UiText? = null,
    val isLoading: Boolean = false,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val auth: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onPhoneChange(value: String) {
        val digits = value.filter { it.isDigit() }.take(AppConfig.PHONE_LENGTH)
        _state.update { it.copy(phone = digits, error = null) }
    }

    fun onContinue(onOtpSent: (String) -> Unit) {
        val phone = _state.value.phone
        val error = when {
            phone.isBlank() -> UiText.Res(R.string.error_enter_mobile)
            !PhoneValidator.isValid(phone) -> UiText.Res(R.string.error_invalid_mobile)
            else -> null
        }
        if (error != null) {
            _state.update { it.copy(error = error) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            auth.requestOtp(phone)
                .onSuccess {
                    _state.update { s -> s.copy(isLoading = false) }
                    onOtpSent(phone)
                }
                .onFailure { e ->
                    _state.update { s -> s.copy(isLoading = false, error = e.asUiText() ?: UiText.Res(R.string.error_send_otp)) }
                }
        }
    }
}
