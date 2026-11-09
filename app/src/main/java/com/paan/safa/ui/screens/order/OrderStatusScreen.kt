package com.paan.safa.ui.screens.order

import androidx.annotation.StringRes
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.paan.safa.R
import com.paan.safa.core.ui.UiText
import com.paan.safa.core.ui.asString
import com.paan.safa.core.util.AppConfig
import com.paan.safa.core.util.asOrderTime
import com.paan.safa.core.util.asRupees
import com.paan.safa.domain.model.Order
import com.paan.safa.domain.model.OrderStatus
import com.paan.safa.domain.model.PaymentStatus
import com.paan.safa.ui.components.EmptyState
import com.paan.safa.ui.components.OrderStatusSkeleton
import com.paan.safa.ui.components.OrderStatusTimeline
import com.paan.safa.ui.components.PaanBackground
import com.paan.safa.ui.components.PrimaryPaanButton
import com.paan.safa.ui.theme.Dimens
import com.paan.safa.ui.theme.PaanColors
import com.paan.safa.ui.theme.PaanTextStyles
import com.paan.safa.ui.theme.PillShape
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.paan.safa.domain.model.displayName
import com.paan.safa.domain.model.callNumber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderStatusScreen(
    onBack: () -> Unit,
    onHome: () -> Unit,
    viewModel: OrderStatusViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val order = state.order
    val snackbar = remember { SnackbarHostState() }
    var scanning by remember { mutableStateOf(false) }
    val message = state.message?.asString()

    LaunchedEffect(message) {
        message?.let {
            snackbar.showSnackbar(it)
            viewModel.messageShown()
        }
    }

    fun scanPickupQr() {
        if (scanning) return
        scanning = true
        startPickupScan(
            context = context,
            onResult = { scanning = false; viewModel.onScanned(it) },
            onError = { scanning = false; viewModel.showMessage(it) },
            onCancel = { scanning = false },
        )
    }

    fun callShop() {
        val phone = state.shop.callNumber
        if (phone == null) {
            viewModel.showMessage(UiText.Res(R.string.orders_shop_phone_unavailable))
            return
        }
        try {
            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
        } catch (_: ActivityNotFoundException) { /* device has no dialer */ }
    }

    Scaffold(
        containerColor = PaanColors.Cream,
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbar, Modifier.navigationBarsPadding()) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(order?.orderNumber ?: stringResource(R.string.orders_default_title), style = MaterialTheme.typography.titleLarge)
                        Text(stringResource(R.string.orders_live_status), style = MaterialTheme.typography.labelSmall, color = PaanColors.MutedBrown)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.a11y_back)) }
                },
                actions = {
                    IconButton(onClick = ::callShop) { Icon(Icons.Filled.SupportAgent, stringResource(R.string.a11y_call_shop)) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PaanColors.Cream),
            )
        },
    ) { padding ->
        PaanBackground(Modifier.padding(padding)) {
            when {
                state.isLoading -> OrderStatusSkeleton()
                order == null -> EmptyState(
                    title = stringResource(R.string.orders_not_found),
                    message = state.error?.asString() ?: stringResource(R.string.orders_not_found_body),
                    actionLabel = stringResource(R.string.common_back_to_home),
                    onAction = onHome,
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(Dimens.ScreenMargin),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
                ) {
                    item { StatusHero(order.status) }
                    if (order.status == OrderStatus.READY) {
                        item { PickupScanCard(busy = state.pickupBusy, onScan = ::scanPickupQr) }
                    }
                    item { CollectionPass(order, state.customerPhone, state.shop.displayName) }
                    item { PaymentStatusCard(order) }
                    item { JourneyCard(order) }
                    item { SummaryCard(order) }
                    item { PolicyCard() }
                    item {
                        OutlinedButton(
                            onClick = ::callShop,
                            modifier = Modifier.fillMaxWidth().height(Dimens.ButtonHeight),
                            shape = PillShape,
                            border = BorderStroke(1.5.dp, PaanColors.RoyalGold),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PaanColors.ForestGreen),
                        ) {
                            Icon(Icons.Filled.Call, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.orders_call_shop))
                        }
                    }
                    item { PrimaryPaanButton(stringResource(R.string.common_back_to_home), onClick = onHome, modifier = Modifier.fillMaxWidth()) }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------

private data class HeroStyle(
    val container: androidx.compose.ui.graphics.Color,
    val onContainer: androidx.compose.ui.graphics.Color,
    @StringRes val label: Int,
    @StringRes val headline: Int,
    @StringRes val body: Int,
)

@Composable
private fun StatusHero(status: OrderStatus) {
    val s = when (status) {
        OrderStatus.PENDING -> HeroStyle(
            PaanColors.DeepForest, PaanColors.Cream,
            R.string.hero_pending_label, R.string.hero_pending_headline,
            R.string.hero_pending_body,
        )
        OrderStatus.MAKING -> HeroStyle(
            PaanColors.ForestGreen, PaanColors.Cream,
            R.string.hero_making_label, R.string.hero_making_headline,
            R.string.hero_making_body,
        )
        OrderStatus.READY -> HeroStyle(
            PaanColors.RoyalGold, PaanColors.DarkBrown,
            R.string.hero_ready_label, R.string.hero_ready_headline,
            R.string.hero_ready_body,
        )
        OrderStatus.COMPLETED -> HeroStyle(
            PaanColors.DeepForest, PaanColors.Cream,
            R.string.hero_completed_label, R.string.hero_completed_headline,
            R.string.hero_completed_body,
        )
        OrderStatus.ADMIN_CANCELLED -> HeroStyle(
            PaanColors.Maroon, PaanColors.Cream,
            R.string.hero_cancelled_label, R.string.hero_cancelled_headline,
            R.string.hero_cancelled_body,
        )
        OrderStatus.PAYMENT_FAILED -> HeroStyle(
            PaanColors.Maroon, PaanColors.Cream,
            R.string.hero_payment_failed_label, R.string.hero_payment_failed_headline,
            R.string.hero_payment_failed_body,
        )
    }
    Card(
        Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite }, // TalkBack announces changes
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = s.container),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(stringResource(s.label), style = MaterialTheme.typography.labelMedium, color = s.onContainer.copy(alpha = 0.85f))
            Text(
                stringResource(s.headline),
                style = if (status == OrderStatus.READY) MaterialTheme.typography.headlineLarge
                else MaterialTheme.typography.headlineMedium,
                color = s.onContainer,
            )
            Text(stringResource(s.body), style = MaterialTheme.typography.bodyMedium, color = s.onContainer)
        }
    }
}

@Composable
private fun CollectionPass(order: Order, phone: String, shopName: String) {
    Card(
        Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = PaanColors.DeepForest),
        border = BorderStroke(1.dp, PaanColors.RoyalGold.copy(alpha = 0.6f)),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(stringResource(R.string.orders_counter_pass), style = MaterialTheme.typography.labelMedium, color = PaanColors.LightGold)
            Text(stringResource(R.string.orders_token, order.token), style = PaanTextStyles.TokenCode, color = PaanColors.Cream)
            Text(shopName, style = MaterialTheme.typography.bodyMedium, color = PaanColors.Cream)
            if (phone.isNotBlank()) {
                Text(stringResource(R.string.orders_phone, phone), style = MaterialTheme.typography.labelMedium, color = PaanColors.LightGold)
            }
            Text(
                stringResource(R.string.orders_placed_at, order.createdAt.asOrderTime()),
                style = MaterialTheme.typography.labelSmall,
                color = PaanColors.LightGold,
            )
        }
    }
}

@Composable
private fun PaymentStatusCard(order: Order) {
    val failed = order.paymentStatus == PaymentStatus.FAILED
    Card(
        Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = PaanColors.CardWhite),
        border = BorderStroke(1.dp, PaanColors.RoyalGold.copy(alpha = 0.25f)),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (failed) Icons.Filled.Cancel else Icons.Filled.CheckCircle,
                null,
                tint = if (failed) PaanColors.Maroon else PaanColors.FreshGreen,
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(stringResource(R.string.orders_payment_status), style = MaterialTheme.typography.labelMedium, color = PaanColors.MutedBrown)
                Text(
                    stringResource(R.string.orders_payment_summary, stringResource(order.paymentStatus.labelRes), order.total.asRupees(), stringResource(order.paymentMethod.labelRes)),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

@Composable
private fun JourneyCard(order: Order) {
    Card(
        Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = PaanColors.CardWhite),
        border = BorderStroke(1.dp, PaanColors.RoyalGold.copy(alpha = 0.25f)),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)) {
            Column {
                Text(stringResource(R.string.orders_preparation_journey), style = MaterialTheme.typography.titleLarge)
                Text(stringResource(R.string.orders_workflow), style = MaterialTheme.typography.labelSmall, color = PaanColors.MutedBrown)
            }
            if (order.status == OrderStatus.ADMIN_CANCELLED || order.status == OrderStatus.PAYMENT_FAILED) {
                val noticeRes = if (order.status == OrderStatus.PAYMENT_FAILED)
                    R.string.orders_payment_failed_notice
                else
                    R.string.orders_cancelled_notice
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Filled.Info, null, tint = PaanColors.Maroon)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(noticeRes),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            } else {
                OrderStatusTimeline(order.status)
            }
        }
    }
}

@Composable
private fun SummaryCard(order: Order) {
    Card(
        Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = PaanColors.CardWhite),
        border = BorderStroke(1.dp, PaanColors.RoyalGold.copy(alpha = 0.25f)),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.common_order_summary), style = MaterialTheme.typography.titleLarge)
                Text(order.total.asRupees(), style = PaanTextStyles.Currency)
            }
            order.items.forEach { item ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.checkout_item_line, item.quantity, item.name), style = MaterialTheme.typography.bodyMedium)
                        Text(item.options.summary(LocalContext.current), style = MaterialTheme.typography.labelSmall, color = PaanColors.MutedBrown)
                    }
                    Text(item.lineTotal.asRupees(), style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (order.comment.isNotBlank()) {
                HorizontalDivider(color = PaanColors.RoyalGold.copy(alpha = 0.3f))
                Text(stringResource(R.string.orders_preparation_note), style = MaterialTheme.typography.labelMedium, color = PaanColors.MutedBrown)
                Text(stringResource(R.string.common_quoted, order.comment), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun PolicyCard() {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Icon(Icons.Filled.Schedule, null, Modifier.size(18.dp), tint = PaanColors.RoyalGold)
        Spacer(Modifier.width(8.dp))
        Text(
            stringResource(R.string.orders_pickup_note),
            style = MaterialTheme.typography.bodySmall,
            color = PaanColors.MutedBrown,
        )
    }
}

@Composable
private fun DevPanel(onNext: () -> Unit, onCancel: () -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = PaanColors.CardWhite),
        border = BorderStroke(1.dp, PaanColors.MutedBrown.copy(alpha = 0.4f)),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.orders_dev_controls), style = MaterialTheme.typography.labelLarge)
            Text(
                stringResource(R.string.orders_dev_hint),
                style = MaterialTheme.typography.labelSmall,
                color = PaanColors.MutedBrown,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onNext,
                    colors = ButtonDefaults.buttonColors(containerColor = PaanColors.ForestGreen, contentColor = PaanColors.Cream),
                ) { Text(stringResource(R.string.orders_next_status)) }
                OutlinedButton(
                    onClick = onCancel,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PaanColors.Maroon),
                ) { Text(stringResource(R.string.orders_cancel_shop)) }
            }
        }
    }
}


@Composable
private fun PickupScanCard(busy: Boolean, onScan: () -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = PaanColors.CardWhite),
        border = BorderStroke(1.dp, PaanColors.RoyalGold.copy(alpha = 0.6f)),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.QrCodeScanner, null, tint = PaanColors.ForestGreen)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.orders_scan_pickup), style = MaterialTheme.typography.titleLarge)
            }
            Text(
                stringResource(R.string.orders_scan_prompt),
                style = MaterialTheme.typography.bodyMedium,
                color = PaanColors.MutedBrown,
            )
            PrimaryPaanButton(
                text = stringResource(R.string.orders_scan_button),
                onClick = onScan,
                modifier = Modifier.fillMaxWidth(),
                isLoading = busy,
            )
        }
    }
}