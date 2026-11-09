package com.paan.safa.ui.screens.product

import com.paan.safa.core.ui.asString
import com.paan.safa.core.ui.UiText
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.paan.safa.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.paan.safa.core.util.AppConfig
import com.paan.safa.core.util.asRupees
import com.paan.safa.domain.model.ItemOptions
import com.paan.safa.domain.model.Product
import com.paan.safa.domain.model.SupariType
import com.paan.safa.domain.model.Sweetness
import com.paan.safa.ui.components.*
import com.paan.safa.ui.theme.Dimens
import com.paan.safa.ui.theme.PaanColors
import com.paan.safa.ui.theme.PaanTextStyles
import com.paan.safa.ui.theme.PillShape

@Composable
fun ProductDetailScreen(
    onBack: () -> Unit,
    onAddMore: () -> Unit,
    onViewCart: () -> Unit,
    viewModel: ProductDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val product = state.product

    Scaffold(
        containerColor = PaanColors.Cream,
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (product != null) {
                BottomActions(
                    total = state.total,
                    enabled = state.canAdd,
                    closed = state.shopClosed,
                    onAddMore = { viewModel.addToCart(onAddMore) },
                    onAddToOrder = { viewModel.addToCart(onViewCart) },
                )
            }
        },
    ) { padding ->
        PaanBackground(Modifier.padding(padding)) {
            when {
                state.isLoading -> ProductDetailSkeleton()
                product == null -> Column(
                    Modifier.align(Alignment.Center).padding(Dimens.SpaceLg),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text((state.error ?: UiText.Res(R.string.error_product_not_found)).asString(), style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(Dimens.SpaceMd))
                    PrimaryPaanButton(stringResource(R.string.common_go_back), onBack)
                }
                else -> DetailContent(
                    product = product,
                    shopName = state.shopName,
                    quantity = state.quantity,
                    total = state.total,
                    options = state.options,
                    onIncrement = viewModel::increment,
                    onDecrement = viewModel::decrement,
                    onSweetness = viewModel::onSweetnessSelected,
                    onSupari = viewModel::onSupariSelected,
                )
            }
            BackButton(onBack, Modifier.align(Alignment.TopStart))
        }
    }
}

@Composable
private fun BackButton(onBack: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(
        onClick = onBack,
        modifier = modifier
            .statusBarsPadding()
            .padding(12.dp)
            .background(PaanColors.Cream.copy(alpha = 0.9f), CircleShape),
    ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.a11y_back), tint = PaanColors.DeepForest) }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailContent(
    product: Product,
    shopName: String,
    quantity: Int,
    total: Int,
    options: ItemOptions,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onSweetness: (Sweetness) -> Unit,
    onSupari: (SupariType) -> Unit,
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ProductImage(product.imageUrl, product.name, Modifier.fillMaxWidth().height(300.dp))

        Column(
            Modifier.padding(Dimens.ScreenMargin),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Storefront, null, Modifier.size(16.dp), tint = PaanColors.MutedBrown)
                Spacer(Modifier.width(4.dp))
                Text(
                    stringResource(R.string.product_pickup_shop, shopName),
                    style = MaterialTheme.typography.labelMedium,
                    color = PaanColors.MutedBrown,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                product.tag?.let {
                    Text(it, style = MaterialTheme.typography.labelMedium, color = PaanColors.RoyalGold)
                }
                Text(product.name, style = MaterialTheme.typography.headlineLarge)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(product.price.asRupees(), style = PaanTextStyles.Currency)
                    Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.product_per_piece), style = MaterialTheme.typography.bodySmall, color = PaanColors.MutedBrown)
                }
                if (!product.isAvailable) {
                    Text(stringResource(R.string.product_unavailable), style = MaterialTheme.typography.labelLarge, color = PaanColors.Maroon)
                }
            }

            GoldDivider(Modifier.align(Alignment.CenterHorizontally))

            Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
            Text(stringResource(R.string.product_experience), style = MaterialTheme.typography.titleLarge)
                Text(product.description, style = MaterialTheme.typography.bodyLarge, color = PaanColors.DarkBrown)
            }

            if (product.ingredients.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
            Text(stringResource(R.string.product_heritage), style = MaterialTheme.typography.titleLarge)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        product.ingredients.forEach { IngredientChip(it) }
                    }
                }
            }

            if (product.isAvailable) {
                // Sweetness & Gulkand
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
                    SectionHeader(stringResource(R.string.product_sweetness_header), stringResource(R.string.product_choose_one))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Sweetness.entries.forEach { s ->
                            OptionChip(stringResource(s.labelRes), selected = options.sweetness == s) { onSweetness(s) }
                        }
                    }
                }

                // Supari type
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
                    SectionHeader(stringResource(R.string.product_supari_header), stringResource(R.string.product_choose_one))
                    SupariType.entries.forEach { t ->
                        OptionCard(
                            title = stringResource(t.labelRes),
                            subtitle = stringResource(t.noteRes),
                            selected = options.supari == t,
                            onClick = { onSupari(t) },
                        )
                    }
                }

                Text(
                    stringResource(R.string.product_same_price),
                    style = MaterialTheme.typography.labelSmall,
                    color = PaanColors.FreshGreen,
                )
            }

            QuantityCard(quantity, product.price, total, product.isAvailable, onIncrement, onDecrement)

            Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Filled.Verified, null, Modifier.size(20.dp), tint = PaanColors.FreshGreen)
                Spacer(Modifier.width(8.dp))
                Column {
                Text(stringResource(R.string.product_made_fresh), style = MaterialTheme.typography.titleMedium)
                    Text(
                        stringResource(R.string.product_handrolled_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = PaanColors.MutedBrown,
                    )
                    Row(Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Schedule, null, Modifier.size(14.dp), tint = PaanColors.RoyalGold)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            stringResource(R.string.product_express_ready),
                            style = MaterialTheme.typography.labelSmall,
                            color = PaanColors.MutedBrown,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, hint: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Text(hint, style = MaterialTheme.typography.labelSmall, color = PaanColors.MutedBrown)
    }
}

@Composable
private fun IngredientChip(label: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = PaanColors.CardWhite,
        border = BorderStroke(1.dp, PaanColors.RoyalGold.copy(alpha = 0.4f)),
    ) {
        Text(label, Modifier.padding(horizontal = 12.dp, vertical = 8.dp), style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun OptionChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelLarge) },
        leadingIcon = if (selected) {
            { Icon(Icons.Filled.Check, null, Modifier.size(16.dp)) }
        } else null,
        shape = MaterialTheme.shapes.small,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = PaanColors.CardWhite,
            labelColor = PaanColors.DarkBrown,
            selectedContainerColor = PaanColors.ForestGreen.copy(alpha = 0.12f),
            selectedLabelColor = PaanColors.ForestGreen,
            selectedLeadingIconColor = PaanColors.ForestGreen,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = PaanColors.MutedBrown.copy(alpha = 0.25f),
            selectedBorderColor = PaanColors.ForestGreen,
            selectedBorderWidth = 1.5.dp,
        ),
    )
}

@Composable
private fun OptionCard(title: String, subtitle: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = if (selected) PaanColors.ForestGreen.copy(alpha = 0.06f) else PaanColors.CardWhite,
        border = BorderStroke(
            if (selected) 2.dp else 1.dp,
            if (selected) PaanColors.ForestGreen else PaanColors.MutedBrown.copy(alpha = 0.25f),
        ),
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (selected) PaanColors.ForestGreen else PaanColors.DarkBrown,
                )
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = PaanColors.MutedBrown)
            }
            Icon(
                if (selected) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                contentDescription = stringResource(if (selected) R.string.a11y_selected else R.string.a11y_not_selected),
                tint = if (selected) PaanColors.ForestGreen else PaanColors.MutedBrown.copy(alpha = 0.5f),
            )
        }
    }
}

@Composable
private fun QuantityCard(
    quantity: Int,
    unitPrice: Int,
    total: Int,
    enabled: Boolean,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
) {
    Card(
        Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = PaanColors.CardWhite),
        border = BorderStroke(1.dp, PaanColors.RoyalGold.copy(alpha = 0.3f)),
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
        Text(stringResource(R.string.common_quantity), style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(R.string.product_quantity_total, quantity, unitPrice.asRupees(), total.asRupees()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = PaanColors.MutedBrown,
                )
            }
            if (enabled) QuantitySelector(quantity, onIncrement, onDecrement)
        }
    }
}

@Composable
private fun BottomActions(
    total: Int,
    enabled: Boolean,
    closed: Boolean,
    onAddMore: () -> Unit,
    onAddToOrder: () -> Unit,
) {
    Surface(color = PaanColors.CardWhite, shadowElevation = 12.dp) {
        Row(
            Modifier
                .navigationBarsPadding()
                .padding(horizontal = Dimens.ScreenMargin, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (closed) {
                PrimaryPaanButton(
                    text = stringResource(R.string.common_shop_closed_now),
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.weight(1f),
                )
            } else {
                OutlinedButton(
                    onClick = onAddMore,
                    enabled = enabled,
                    shape = PillShape,
                    modifier = Modifier.height(Dimens.ButtonHeight),
                    border = BorderStroke(
                        1.5.dp,
                        if (enabled) PaanColors.RoyalGold else PaanColors.MutedBrown.copy(alpha = 0.3f),
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PaanColors.ForestGreen),
            ) { Text(stringResource(R.string.product_add_more)) }
                PrimaryPaanButton(
                    text = stringResource(R.string.product_add_to_order, total.asRupees()),
                    onClick = onAddToOrder,
                    enabled = enabled,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
