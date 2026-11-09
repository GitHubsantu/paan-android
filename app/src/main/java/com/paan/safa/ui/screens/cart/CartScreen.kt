package com.paan.safa.ui.screens.cart

import androidx.compose.ui.res.pluralStringResource
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.paan.safa.R
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.paan.safa.core.util.AppConfig
import com.paan.safa.core.util.asRupees
import com.paan.safa.domain.model.CartItem
import com.paan.safa.ui.components.*
import com.paan.safa.ui.theme.Dimens
import com.paan.safa.ui.theme.PaanColors
import com.paan.safa.ui.theme.PaanTextStyles
import com.paan.safa.ui.theme.PillShape
import com.paan.safa.domain.model.ShopInfo
import com.paan.safa.domain.model.displayName

// The chip label follows the app language; the note text sent to the shop stays English on purpose
// (shop staff read it, and it keeps the "already added" check working after a language switch).
private val QuickNotes = listOf(
    R.string.cart_quick_ice_pack to "Ice pack box",
    R.string.cart_quick_extra_crisp to "Extra betel crisp",
    R.string.cart_quick_less_gulkand to "Less gulkand",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onBack: () -> Unit,
    onAddMore: () -> Unit,
    onCheckout: () -> Unit,
    onTabSelected: (BottomTab) -> Unit,
    viewModel: CartViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.imePadding(),
        containerColor = PaanColors.Cream,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.cart_title), style = MaterialTheme.typography.titleLarge)
                        Text(stringResource(R.string.cart_pickup_only), style = MaterialTheme.typography.labelSmall, color = PaanColors.FreshGreen)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.a11y_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PaanColors.Cream),
            )
        },
        bottomBar = {
            Column {
                if (!state.isEmpty) CheckoutBar(state.itemCount, state.total, state.shopClosed, onCheckout)
                PaanBottomBar(BottomTab.Cart, state.itemCount, onSelected = onTabSelected)
            }
        },
    ) { padding ->
        PaanBackground(Modifier.padding(padding)) {
            if (state.isEmpty) {
                EmptyState(
                    title = stringResource(R.string.cart_empty_title),
                    message = stringResource(R.string.cart_empty_body),
                    actionLabel = stringResource(R.string.cart_browse),
                    onAction = onAddMore,
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(Dimens.ScreenMargin),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
                ) {
                    item { PickupInfoCard(state.shop) }

                    item {
                        Text(
                            stringResource(
                                R.string.cart_crafted_items,
                                pluralStringResource(R.plurals.cart_types, state.typeCount, state.typeCount),
                                pluralStringResource(R.plurals.paans, state.itemCount, state.itemCount),
                            ),
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }

                    items(state.items, key = { it.lineId }) { item ->
                        CartItemCard(
                            item = item,
                            onIncrement = { viewModel.increment(item) },
                            onDecrement = { viewModel.decrement(item) },
                            onRemove = { viewModel.remove(item) },
                        )
                    }

                    item {
                        OutlinedButton(
                            onClick = onAddMore,
                            modifier = Modifier.fillMaxWidth().height(Dimens.ButtonHeight),
                            shape = PillShape,
                            border = BorderStroke(1.5.dp, PaanColors.RoyalGold),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PaanColors.ForestGreen),
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.cart_add_more_types))
                        }
                    }

                    item {
                        CommentSection(
                            comment = state.comment,
                            onChange = viewModel::onCommentChange,
                            onQuickNote = viewModel::addQuickNote,
                        )
                    }

                    item { BillCard(state.itemCount, state.total) }

                    item {
                        Text(
                            stringResource(R.string.cart_handcrafted_note),
                            style = MaterialTheme.typography.bodySmall,
                            color = PaanColors.MutedBrown,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PickupInfoCard(shop: ShopInfo?) {
    Card(
        Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = PaanColors.DeepForest),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Storefront, null, Modifier.size(20.dp), tint = PaanColors.LightGold)
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(stringResource(R.string.cart_express_pickup), style = MaterialTheme.typography.titleMedium, color = PaanColors.Cream)
                    Text(shop.displayName, style = MaterialTheme.typography.bodySmall, color = PaanColors.LightGold)
                }
            }
            shop?.pickupMessage?.takeIf { it.isNotBlank() }?.let { message ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Schedule, null, Modifier.size(16.dp), tint = PaanColors.LightGold)
                    Spacer(Modifier.width(8.dp))
                    Text(message, style = MaterialTheme.typography.labelMedium, color = PaanColors.Cream)
                }
            }
        }
    }
}

@Composable
private fun CartItemCard(
    item: CartItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit,
) {
    Card(
        Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = PaanColors.CardWhite),
        border = BorderStroke(1.dp, PaanColors.RoyalGold.copy(alpha = 0.25f)),
    ) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ProductImage(
                url = item.product.listImageUrl,
                contentDescription = item.product.name,
                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(12.dp)),
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        item.product.name,
                        Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    IconButton(onClick = onRemove, Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Delete, stringResource(R.string.cart_remove_item, item.product.name), tint = PaanColors.Maroon)
                    }
                }
                Text(item.options.summary(LocalContext.current), style = MaterialTheme.typography.bodySmall, color = PaanColors.MutedBrown)
                Text(
                    stringResource(R.string.cart_price_each, item.product.price.asRupees()),
                    style = MaterialTheme.typography.labelSmall,
                    color = PaanColors.MutedBrown,
                )
                Row(
                    Modifier.fillMaxWidth().padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    QuantitySelector(item.quantity, onIncrement, onDecrement)
                    Text(item.lineTotal.asRupees(), style = PaanTextStyles.Currency)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CommentSection(
    comment: String,
    onChange: (String) -> Unit,
    onQuickNote: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.cart_note_title), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.cart_optional), style = MaterialTheme.typography.labelMedium, color = PaanColors.MutedBrown)
        }
        OutlinedTextField(
            value = comment,
            onValueChange = onChange,
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5,
            placeholder = { Text(stringResource(R.string.cart_note_placeholder)) },
            supportingText = {
                Text(
                    stringResource(R.string.cart_note_counter, comment.length, AppConfig.MAX_COMMENT_LENGTH),
                    Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End,
                )
            },
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = PaanColors.CardWhite,
                unfocusedContainerColor = PaanColors.CardWhite,
                focusedBorderColor = PaanColors.ForestGreen,
                cursorColor = PaanColors.ForestGreen,
            ),
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            QuickNotes.forEach { (label, note) ->
                AssistChip(
                    onClick = { onQuickNote(note) },
                    label = { Text(stringResource(label), style = MaterialTheme.typography.labelMedium) },
                    shape = MaterialTheme.shapes.small,
                    border = AssistChipDefaults.assistChipBorder(
                        enabled = true,
                        borderColor = PaanColors.RoyalGold.copy(alpha = 0.5f),
                    ),
                    colors = AssistChipDefaults.assistChipColors(containerColor = PaanColors.CardWhite),
                )
            }
        }
    }
}

@Composable
private fun BillCard(itemCount: Int, total: Int) {
    Card(
        Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = PaanColors.CardWhite),
        border = BorderStroke(1.dp, PaanColors.RoyalGold.copy(alpha = 0.25f)),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringResource(R.string.cart_bill_details), style = MaterialTheme.typography.titleLarge)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    stringResource(R.string.cart_item_total, pluralStringResource(R.plurals.paans, itemCount, itemCount)),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(total.asRupees(), style = MaterialTheme.typography.bodyMedium)
            }
            HorizontalDivider(color = PaanColors.RoyalGold.copy(alpha = 0.4f))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(stringResource(R.string.cart_total_amount), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.cart_pickup_counter), style = MaterialTheme.typography.labelSmall, color = PaanColors.MutedBrown)
                }
                Text(total.asRupees(), style = PaanTextStyles.Currency)
            }
        }
    }
}

@Composable
private fun CheckoutBar(itemCount: Int, total: Int, closed: Boolean, onCheckout: () -> Unit) {
    Surface(color = PaanColors.CardWhite, shadowElevation = 12.dp) {
        Row(
            Modifier
                .padding(horizontal = Dimens.ScreenMargin, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column {
                Text(total.asRupees(), style = PaanTextStyles.Currency)
                Text(
                    pluralStringResource(R.plurals.items, itemCount, itemCount),
                    style = MaterialTheme.typography.labelSmall,
                    color = PaanColors.MutedBrown,
                )
            }
            PrimaryPaanButton(
                text = if (closed) stringResource(R.string.common_shop_closed_now) else stringResource(R.string.cart_proceed),
                onClick = onCheckout,
                enabled = !closed,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
