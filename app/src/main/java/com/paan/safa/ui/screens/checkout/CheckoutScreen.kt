package com.paan.safa.ui.screens.checkout

import androidx.compose.ui.res.pluralStringResource
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.paan.safa.R
import com.paan.safa.core.ui.asString
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.paan.safa.core.util.AppConfig
import com.paan.safa.core.util.asRupees
import com.paan.safa.domain.model.CartItem
import com.paan.safa.domain.model.PaymentMethod
import com.paan.safa.ui.components.EmptyState
import com.paan.safa.ui.components.PaanBackground
import com.paan.safa.ui.components.PaymentMethodCard
import com.paan.safa.ui.components.PrimaryPaanButton
import com.paan.safa.ui.theme.Dimens
import com.paan.safa.ui.theme.PaanColors
import com.paan.safa.ui.theme.PaanTextStyles

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onBack: () -> Unit,
    onPaid: (orderId: String) -> Unit,
    viewModel: CheckoutViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Block the back button while money is being processed.
    BackHandler(enabled = state.isProcessing) {}

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = PaanColors.Cream,
            contentWindowInsets = WindowInsets(0),
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.checkout_title), style = MaterialTheme.typography.titleLarge) },
                    navigationIcon = {
                        IconButton(onClick = onBack, enabled = !state.isProcessing) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.a11y_back))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = PaanColors.Cream),
                )
            },
            bottomBar = {
                if (!state.isEmpty) {
                    PayBar(state.total, enabled = !state.isProcessing, closed = state.shopClosed) { viewModel.pay(onPaid) }
                }
            },
        ) { padding ->
            PaanBackground(Modifier.padding(padding)) {
                if (state.isEmpty) {
                    EmptyState(
                        title = stringResource(R.string.checkout_empty_title),
                        message = stringResource(R.string.checkout_empty_body),
                        actionLabel = stringResource(R.string.common_back),
                        onAction = onBack,
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(Dimens.ScreenMargin),
                        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
                    ) {
                        state.error?.let { msg -> item { ErrorBanner(msg.asString()) } }

                        item { PickupRow(state.shopName) }

                        item { SectionTitle(stringResource(R.string.checkout_order_items)) }
                        item { OrderItemsCard(state.items) }

                        item { SectionTitle(stringResource(R.string.checkout_your_note)) }
                        item { NoteCard(state.comment, onEdit = onBack) }

                        item { SectionTitle(stringResource(R.string.checkout_payment_method)) }
                        items(PaymentMethod.entries, key = { it.name }) { method ->
                            PaymentMethodCard(
                                method = method,
                                selected = state.method == method,
                                onClick = { viewModel.onMethodSelected(method) },
                            )
                        }

                        item { TotalCard(state.itemCount, state.total) }
                    }
                }
            }
        }

        if (state.isProcessing) ProcessingOverlay()
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleLarge)
}

@Composable
private fun PickupRow(shopName: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.Storefront, null, Modifier.size(18.dp), tint = PaanColors.ForestGreen)
        Spacer(Modifier.width(8.dp))
        Text(
            stringResource(R.string.checkout_pickup_from, shopName),
            style = MaterialTheme.typography.bodyMedium,
            color = PaanColors.MutedBrown,
        )
    }
}

@Composable
private fun ErrorBanner(message: String) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = PaanColors.Error.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, PaanColors.Error.copy(alpha = 0.5f)),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.ErrorOutline, null, tint = PaanColors.Error)
            Spacer(Modifier.width(8.dp))
            Text(message, style = MaterialTheme.typography.bodyMedium, color = PaanColors.Error)
        }
    }
}

@Composable
private fun OrderItemsCard(items: List<CartItem>) {
    Card(
        Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = PaanColors.CardWhite),
        border = BorderStroke(1.dp, PaanColors.RoyalGold.copy(alpha = 0.25f)),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items.forEachIndexed { index, item ->
                if (index > 0) HorizontalDivider(color = PaanColors.RoyalGold.copy(alpha = 0.25f))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.checkout_item_line, item.quantity, item.product.name), style = MaterialTheme.typography.titleMedium)
                        Text(
                            item.options.summary(LocalContext.current),
                            style = MaterialTheme.typography.bodySmall,
                            color = PaanColors.MutedBrown,
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(item.lineTotal.asRupees(), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
private fun NoteCard(comment: String, onEdit: () -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = PaanColors.CardWhite),
        border = BorderStroke(1.dp, PaanColors.RoyalGold.copy(alpha = 0.25f)),
    ) {
        Row(Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (comment.isBlank()) stringResource(R.string.checkout_no_note) else stringResource(R.string.common_quoted, comment),
                Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = if (comment.isBlank()) PaanColors.MutedBrown else PaanColors.DarkBrown,
            )
                    TextButton(onClick = onEdit) { Text(stringResource(R.string.common_edit), color = PaanColors.ForestGreen) }
        }
    }
}

@Composable
private fun TotalCard(itemCount: Int, total: Int) {
    Card(
        Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = PaanColors.CardWhite),
        border = BorderStroke(1.dp, PaanColors.RoyalGold.copy(alpha = 0.25f)),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                    Text(stringResource(R.string.cart_total_amount), style = MaterialTheme.typography.titleMedium)
                Text(
                    pluralStringResource(R.plurals.paans, itemCount, itemCount),
                    style = MaterialTheme.typography.labelSmall,
                    color = PaanColors.MutedBrown,
                )
            }
            Text(total.asRupees(), style = PaanTextStyles.Currency)
        }
    }
}

@Composable
private fun PayBar(total: Int, enabled: Boolean, closed: Boolean, onPay: () -> Unit) {
    Surface(color = PaanColors.CardWhite, shadowElevation = 12.dp) {
        PrimaryPaanButton(
            text = if (closed) stringResource(R.string.common_shop_closed_now) else stringResource(R.string.checkout_pay, total.asRupees()),
            onClick = onPay,
            enabled = enabled && !closed,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = Dimens.ScreenMargin, vertical = 12.dp),
        )
    }
}

@Composable
private fun ProcessingOverlay() {
    Box(
        Modifier
            .fillMaxSize()
            .background(PaanColors.DeepForest.copy(alpha = 0.94f))
            .pointerInput(Unit) {},   // swallow touches while paying
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            CircularProgressIndicator(color = PaanColors.RoyalGold)
            Text(stringResource(R.string.checkout_processing), style = MaterialTheme.typography.titleLarge, color = PaanColors.Cream)
            Text(
                stringResource(R.string.checkout_dont_close),
                style = MaterialTheme.typography.bodyMedium,
                color = PaanColors.LightGold,
            )
        }
    }
}
