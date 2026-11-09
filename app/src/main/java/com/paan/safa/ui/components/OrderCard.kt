package com.paan.safa.ui.components

import androidx.compose.ui.res.pluralStringResource
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.paan.safa.R
import com.paan.safa.core.util.asOrderTime
import com.paan.safa.core.util.asRupees
import com.paan.safa.domain.model.Order
import com.paan.safa.domain.model.OrderStatus
import com.paan.safa.ui.theme.PaanColors
import com.paan.safa.ui.theme.PaanTextStyles
import com.paan.safa.ui.theme.PillShape

@Composable
fun OrderCard(order: Order, onViewDetails: () -> Unit, modifier: Modifier = Modifier) {
    val ready = order.status == OrderStatus.READY
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onViewDetails),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = PaanColors.CardWhite),
        border = BorderStroke(
            if (ready) 2.dp else 1.dp,
            if (ready) PaanColors.RoyalGold else PaanColors.RoyalGold.copy(alpha = 0.25f),
        ),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(order.orderNumber, style = MaterialTheme.typography.titleMedium)
                    Text(order.createdAt.asOrderTime(), style = MaterialTheme.typography.labelSmall, color = PaanColors.MutedBrown)
                }
                OrderStatusChip(order.status)
            }

            Text(
                order.items.joinToString(", ") { "${it.quantity} × ${it.name}" },
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            if (ready) {
                Text(
                    stringResource(R.string.order_card_ready_token, order.token),
                    style = MaterialTheme.typography.labelLarge,
                    color = PaanColors.ForestGreen,
                )
            }

            HorizontalDivider(color = PaanColors.RoyalGold.copy(alpha = 0.3f))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(order.total.asRupees(), style = PaanTextStyles.Currency)
                    Text(
                        pluralStringResource(R.plurals.paans, order.itemCount, order.itemCount),
                        style = MaterialTheme.typography.labelSmall,
                        color = PaanColors.MutedBrown,
                    )
                }
                OutlinedButton(
                    onClick = onViewDetails,
                    shape = PillShape,
                    border = BorderStroke(1.5.dp, PaanColors.ForestGreen),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PaanColors.ForestGreen),
                ) { Text(stringResource(R.string.common_view_details)) }
            }
        }
    }
}
