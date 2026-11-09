package com.paan.safa.ui.screens.payment

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.core.content.ContextCompat
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.toArgb
import com.paan.safa.R
import com.paan.safa.core.ui.asString
import com.paan.safa.ui.components.PaanBackground
import com.paan.safa.ui.components.PrimaryPaanButton
import com.paan.safa.ui.theme.Dimens
import com.paan.safa.ui.theme.PaanColors

@Composable
fun PaymentScreen(
    onPaid: (orderId: String) -> Unit,
    onBack: () -> Unit,
    viewModel: PaymentViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) { viewModel.start() }

    // Open the gateway's hosted checkout page in a Custom Tab.
    LaunchedEffect(Unit) {
        viewModel.openCheckout.collect { url ->
            val colors = CustomTabColorSchemeParams.Builder()
                .setToolbarColor(PaanColors.DeepForest.toArgb())
                .setNavigationBarColor(PaanColors.DeepForest.toArgb())
                .build()

            val intent = CustomTabsIntent.Builder()
                .setDefaultColorSchemeParams(colors)
                .setShowTitle(true)
                .setShareState(CustomTabsIntent.SHARE_STATE_OFF)
                .setUrlBarHidingEnabled(true)
                .setCloseButtonIcon(context.drawableToBitmap(R.drawable.ic_close_back))
                .build()

            intent.launchUrl(context, Uri.parse(url))
        }
    }

    // Back from the Custom Tab / UPI app: confirm with our server.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.onResume() }

    LaunchedEffect(state) { (state as? PaymentUiState.Paid)?.let { onPaid(it.orderId) } }

    PaanBackground {
        Column(
            Modifier.fillMaxSize().systemBarsPadding().padding(Dimens.ScreenMargin),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            when (val s = state) {
                PaymentUiState.Idle, PaymentUiState.Starting ->
                    Waiting(stringResource(R.string.payment_preparing))

                PaymentUiState.InGateway -> {
                    Waiting(stringResource(R.string.payment_in_browser))
                    Spacer(Modifier.height(Dimens.SpaceLg))
                    PrimaryPaanButton(stringResource(R.string.payment_check_status), viewModel::checkAgain, Modifier.fillMaxWidth())
                    TextButton(onClick = viewModel::retry) {
                        Text(stringResource(R.string.payment_reopen), color = PaanColors.ForestGreen)
                    }
                }

                PaymentUiState.Confirming -> {
                    Waiting(stringResource(R.string.payment_confirming))
                    TextButton(onClick = viewModel::stopWaiting) {
                        Text(stringResource(R.string.payment_stop_waiting), color = PaanColors.ForestGreen)
                    }
                }

                is PaymentUiState.Paid -> {
                    Icon(Icons.Filled.CheckCircle, null, Modifier.size(64.dp), tint = PaanColors.ForestGreen)
                    Spacer(Modifier.height(Dimens.SpaceMd))
                    Text(stringResource(R.string.payment_success), style = MaterialTheme.typography.titleLarge)
                }

                is PaymentUiState.Failed -> {
                    Problem(s.message.asString())
                    PrimaryPaanButton(stringResource(R.string.common_try_again), viewModel::retry, Modifier.fillMaxWidth())
                    TextButton(onClick = onBack) { Text(stringResource(R.string.common_back), color = PaanColors.ForestGreen) }
                }

                PaymentUiState.Timeout -> {
                    Problem(stringResource(R.string.payment_timeout_body))
                    PrimaryPaanButton(stringResource(R.string.payment_check_again), viewModel::checkAgain, Modifier.fillMaxWidth())
                    TextButton(onClick = viewModel::retry) { Text(stringResource(R.string.payment_pay_again), color = PaanColors.ForestGreen) }
                    TextButton(onClick = onBack) { Text(stringResource(R.string.common_back), color = PaanColors.ForestGreen) }
                }
            }
        }
    }
}

@Composable
private fun Waiting(text: String) {
    CircularProgressIndicator(color = PaanColors.ForestGreen)
    Spacer(Modifier.height(Dimens.SpaceMd))
    Text(text, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
}

@Composable
private fun Problem(text: String) {
    Icon(Icons.Filled.ErrorOutline, null, Modifier.size(56.dp), tint = PaanColors.Error)
    Spacer(Modifier.height(Dimens.SpaceMd))
    Text(text, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
    Spacer(Modifier.height(Dimens.SpaceLg))
}

private fun Context.drawableToBitmap(@DrawableRes id: Int): Bitmap {
    val drawable = ContextCompat.getDrawable(this, id)!!
    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth.coerceAtLeast(1),
        drawable.intrinsicHeight.coerceAtLeast(1),
        Bitmap.Config.ARGB_8888,
    )
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}