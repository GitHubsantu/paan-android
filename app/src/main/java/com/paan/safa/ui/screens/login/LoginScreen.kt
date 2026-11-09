package com.paan.safa.ui.screens.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.paan.safa.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.paan.safa.core.util.AppConfig
import com.paan.safa.core.ui.asString
import com.paan.safa.ui.components.GoldDivider
import com.paan.safa.ui.components.PaanBackground
import com.paan.safa.ui.components.PaanLogo
import com.paan.safa.ui.components.PrimaryPaanButton
import com.paan.safa.ui.theme.Dimens
import com.paan.safa.ui.theme.PaanColors

@Composable
fun LoginScreen(
    onOtpSent: (String) -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    PaanBackground {
        Column(
            Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = Dimens.ScreenMargin, vertical = Dimens.SpaceLg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            PaanLogo(logoSize = 96.dp)
            Spacer(Modifier.height(Dimens.SpaceMd))
            Text(AppConfig.BRAND_NAME, style = MaterialTheme.typography.displayMedium, color = PaanColors.DeepForest)
            Spacer(Modifier.height(Dimens.SpaceSm))
            GoldDivider()
            Spacer(Modifier.height(Dimens.SpaceMd))
            Text(
                stringResource(R.string.login_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = PaanColors.MutedBrown,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(Dimens.SpaceLg))

            OutlinedTextField(
                value = state.phone,
                onValueChange = viewModel::onPhoneChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(stringResource(R.string.login_mobile_number)) },
                placeholder = { Text(stringResource(R.string.login_mobile_placeholder)) },
                prefix = { Text(stringResource(R.string.login_country_prefix)) },
                isError = state.error != null,
                supportingText = state.error?.let { { Text(it.asString()) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { viewModel.onContinue(onOtpSent) }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PaanColors.ForestGreen,
                    focusedLabelColor = PaanColors.ForestGreen,
                    cursorColor = PaanColors.ForestGreen,
                    unfocusedContainerColor = PaanColors.CardWhite,
                    focusedContainerColor = PaanColors.CardWhite,
                ),
            )
            Spacer(Modifier.height(Dimens.SpaceMd))
            PrimaryPaanButton(
                text = stringResource(R.string.login_send_otp),
                onClick = { viewModel.onContinue(onOtpSent) },
                isLoading = state.isLoading,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(Dimens.SpaceLg))
            Text(
                stringResource(R.string.login_terms_notice),
                style = MaterialTheme.typography.bodySmall,
                color = PaanColors.MutedBrown,
                textAlign = TextAlign.Center,
            )
        }
    }
}
