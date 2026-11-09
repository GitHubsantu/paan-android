package com.paan.safa.ui.screens.order

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import com.paan.safa.R
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.paan.safa.core.util.AppConfig
import com.paan.safa.core.util.asOrderTime
import com.paan.safa.core.util.asRupees
import com.paan.safa.domain.model.Order
import com.paan.safa.ui.components.GoldDivider
import com.paan.safa.ui.components.OrderStatusSkeleton
import com.paan.safa.ui.components.PaanBackground
import com.paan.safa.ui.components.PrimaryPaanButton
import com.paan.safa.ui.theme.Dimens
import com.paan.safa.ui.theme.PaanColors
import com.paan.safa.ui.theme.PaanTextStyles
import com.paan.safa.ui.theme.PillShape
import com.paan.safa.domain.model.displayName

@Composable
fun OrderConfirmationScreen(
    onTrackOrder: (orderId: String) -> Unit,
    onHome: () -> Unit,
    viewModel: OrderConfirmationViewModel = hiltViewModel(),
) {
    val order by viewModel.order.collectAsStateWithLifecycle()
    val shop by viewModel.shop.collectAsStateWithLifecycle()

    // Back from here goes Home, never back into checkout.
    BackHandler(onBack = onHome)

    PaanBackground {
        val o = order
        if (o == null) {
            OrderStatusSkeleton()
        } else {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .safeDrawingPadding()
                    .padding(horizontal = Dimens.ScreenMargin, vertical = Dimens.SpaceLg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
            ) {
                SuccessBadge()
                Text(stringResource(R.string.orders_placed), style = MaterialTheme.typography.displayMedium, color = PaanColors.DeepForest)
                Text(
                    stringResource(R.string.orders_show_token),
                    style = MaterialTheme.typography.bodyMedium,
                    color = PaanColors.MutedBrown,
                    textAlign = TextAlign.Center,
                )
                GoldDivider()
                TokenCard(o, shop.displayName)
                SummaryCard(o)

                Spacer(Modifier.height(Dimens.SpaceSm))
                PrimaryPaanButton(stringResource(R.string.orders_track), onClick = { onTrackOrder(o.id) }, modifier = Modifier.fillMaxWidth())
                OutlinedButton(
                    onClick = onHome,
                    modifier = Modifier.fillMaxWidth().height(Dimens.ButtonHeight),
                    shape = PillShape,
                    border = BorderStroke(1.5.dp, PaanColors.RoyalGold),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PaanColors.ForestGreen),
                ) { Text(stringResource(R.string.common_back_to_home)) }
            }
        }
    }
}

@Composable
private fun SuccessBadge() {
    Box(
        Modifier
            .size(88.dp)
            .background(PaanColors.ForestGreen, CircleShape)
            .border(3.dp, PaanColors.RoyalGold, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Filled.Check, contentDescription = stringResource(R.string.a11y_order_placed), tint = PaanColors.Cream, modifier = Modifier.size(48.dp))
    }
}

@Composable
private fun TokenCard(order: Order, shopName: String) {
    Card(
        Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = PaanColors.DeepForest),
        border = BorderStroke(1.dp, PaanColors.RoyalGold.copy(alpha = 0.6f)),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(stringResource(R.string.orders_pickup_token), style = MaterialTheme.typography.labelMedium, color = PaanColors.LightGold)
            Text(stringResource(R.string.orders_token_hash, order.token), style = PaanTextStyles.TokenCode.copy(fontSize = androidx.compose.ui.unit.TextUnit(40f, androidx.compose.ui.unit.TextUnitType.Sp)), color = PaanColors.Cream)
            Text(order.orderNumber, style = MaterialTheme.typography.titleMedium, color = PaanColors.Cream)
            Text(
                stringResource(R.string.common_pair, shopName, order.createdAt.asOrderTime()),
                style = MaterialTheme.typography.labelSmall,
                color = PaanColors.LightGold,
            )
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
            Text(stringResource(R.string.common_order_summary), style = MaterialTheme.typography.titleLarge)
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
                Text(stringResource(R.string.orders_your_note), style = MaterialTheme.typography.labelMedium, color = PaanColors.MutedBrown)
                Text(stringResource(R.string.common_quoted, order.comment), style = MaterialTheme.typography.bodyMedium)
            }
            HorizontalDivider(color = PaanColors.RoyalGold.copy(alpha = 0.3f))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(stringResource(R.string.common_total), style = MaterialTheme.typography.titleMedium)
                    Text(
                        stringResource(R.string.orders_payment_via, stringResource(order.paymentStatus.labelRes), stringResource(order.paymentMethod.labelRes)),
                        style = MaterialTheme.typography.labelSmall,
                        color = PaanColors.FreshGreen,
                    )
                }
                Text(order.total.asRupees(), style = PaanTextStyles.Currency)
            }
            Text(
                "Status: ${stringResource(order.status.labelRes)}",
                style = MaterialTheme.typography.labelMedium,
                color = PaanColors.MutedBrown,
            )
        }
    }
}
