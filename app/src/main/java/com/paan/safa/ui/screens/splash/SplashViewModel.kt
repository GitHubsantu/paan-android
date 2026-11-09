package com.paan.safa.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paan.safa.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(auth: AuthRepository) : ViewModel() {
    /** null until the stored session has been read. */
    val loggedIn: StateFlow<Boolean?> = auth.session
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}