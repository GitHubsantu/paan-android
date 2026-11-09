package com.paan.safa.ui.screens.otp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.paan.safa.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.paan.safa.core.util.AppConfig
import com.paan.safa.core.ui.asString
import com.paan.safa.ui.components.PaanBackground
import com.paan.safa.ui.components.PrimaryPaanButton
import com.paan.safa.ui.theme.Dimens
import com.paan.safa.ui.theme.PaanColors
import com.paan.safa.ui.theme.PaanTextStyles

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpScreen(
    onBack: () -> Unit,
    onVerified: (needsName: Boolean) -> Unit,
    viewModel: OtpViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }

    PaanBackground {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            TopAppBar(
                title = { Text(stringResource(R.string.otp_verify_number)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.a11y_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
            )
            Column(
                Modifier.fillMaxSize().padding(horizontal = Dimens.ScreenMargin),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(Dimens.SpaceLg))
                Text(
                    stringResource(R.string.otp_enter_code, AppConfig.OTP_LENGTH),
                    style = MaterialTheme.typography.bodyMedium,
                    color = PaanColors.MutedBrown,
                )
                Spacer(Modifier.height(Dimens.SpaceXs))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.otp_phone, state.phone), style = MaterialTheme.typography.titleLarge)
                    TextButton(onClick = onBack) { Text(stringResource(R.string.common_change), color = PaanColors.ForestGreen) }
                }
                Spacer(Modifier.height(Dimens.SpaceLg))

                OtpBoxes(
                    value = state.otp,
                    onValueChange = viewModel::onOtpChange,
                    isError = state.error != null,
                    focusRequester = focus,
                )
                state.error?.let {
                    Spacer(Modifier.height(Dimens.SpaceSm))
                    Text(it.asString(), color = PaanColors.Error, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(Modifier.height(Dimens.SpaceLg))
                if (state.canResend) {
                    TextButton(onClick = viewModel::resend) {
                        Text(stringResource(R.string.otp_resend_sms), color = PaanColors.ForestGreen)
                    }
                } else {
                    Text(
                        stringResource(R.string.otp_resend_in, state.secondsLeft),
                        style = MaterialTheme.typography.bodyMedium,
                        color = PaanColors.MutedBrown,
                    )
                }
                Spacer(Modifier.height(Dimens.SpaceLg))
                PrimaryPaanButton(
                    text = stringResource(R.string.otp_verify_continue),
                    onClick = { viewModel.verify(onVerified) },
                    enabled = state.canVerify,
                    isLoading = state.isVerifying,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(Dimens.SpaceMd))
            }
        }
    }
}

/** One hidden text field drawn as N boxes, so paste and autofill still work. */
@Composable
private fun OtpBoxes(
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    focusRequester: FocusRequester,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.focusRequester(focusRequester),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        cursorBrush = SolidColor(androidx.compose.ui.graphics.Color.Transparent),
        decorationBox = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(AppConfig.OTP_LENGTH) { i ->
                    val focused = i == value.length
                    val borderColor = when {
                        isError -> PaanColors.Error
                        focused -> PaanColors.ForestGreen
                        else -> PaanColors.RoyalGold.copy(alpha = 0.5f)
                    }
                    Box(
                        Modifier
                            .size(width = 48.dp, height = 56.dp)
                            .background(PaanColors.CardWhite, RoundedCornerShape(8.dp))
                            .border(BorderStroke(if (focused) 2.dp else 1.dp, borderColor), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(value.getOrNull(i)?.toString() ?: "", style = PaanTextStyles.TokenCode)
                    }
                }
            }
        },
    )
}
