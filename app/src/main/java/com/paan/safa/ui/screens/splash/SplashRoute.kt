package com.paan.safa.ui.screens.splash

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SplashRoute(
    onLoggedIn: () -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    SplashScreen(onFinished = {
        if (viewModel.loggedIn.value == true) onLoggedIn() else onLoggedOut()
    })
}