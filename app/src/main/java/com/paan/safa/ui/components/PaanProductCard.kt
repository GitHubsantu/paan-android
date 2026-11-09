package com.paan.safa.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.paan.safa.R
import com.paan.safa.core.util.asRupees
import com.paan.safa.domain.model.Product
import com.paan.safa.ui.theme.PaanColors
import com.paan.safa.ui.theme.PaanTextStyles
import com.paan.safa.ui.theme.PillShape

@Composable
fun PaanProductCard(
    product: Product,
    quantity: Int,
    onClick: () -> Unit,
    onAdd: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier,
    shopClosed: Boolean = false,
) {
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (shopClosed) Color(0xFFF1EEE6) else PaanColors.CardWhite,
        ),
        border = BorderStroke(1.dp, PaanColors.RoyalGold.copy(alpha = 0.25f)),
    ) {
        Row(
            Modifier.padding(12.dp).alpha(if (shopClosed) 0.7f else 1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ProductImage(
                url = product.imageUrl,
                contentDescription = product.name,
                grayscale = shopClosed,
                modifier = Modifier.size(96.dp).clip(RoundedCornerShape(12.dp)).alpha(if (product.isAvailable) 1f else 0.5f),
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                product.tag?.let {
                    Text(it, style = MaterialTheme.typography.labelSmall, color = PaanColors.RoyalGold)
                }
                Text(product.name, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(
                    product.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = PaanColors.MutedBrown,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    Modifier.fillMaxWidth().padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(product.price.asRupees(), style = PaanTextStyles.Currency)
                    when {
                        shopClosed -> Text(
                            stringResource(R.string.common_closed),
                            style = MaterialTheme.typography.labelMedium,
                            color = PaanColors.MutedBrown,
                        )
                        !product.isAvailable -> Text(
                            stringResource(R.string.product_unavailable_short),
                            style = MaterialTheme.typography.labelMedium,
                            color = PaanColors.Maroon,
                        )
                        quantity > 0 -> QuantitySelector(quantity, onIncrement, onDecrement)
                        else -> Button(
                            onClick = onAdd,
                            shape = PillShape,
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PaanColors.ForestGreen, contentColor = PaanColors.Cream,
                            ),
                        ) {
                            Text(stringResource(R.string.common_add))
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}
