package com.paan.safa.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.paan.safa.R
import com.paan.safa.ui.theme.PaanColors
import com.paan.safa.ui.theme.PillShape

@Composable
fun QuantitySelector(
    quantity: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier.border(1.5.dp, PaanColors.ForestGreen, PillShape),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onDecrement, Modifier.size(40.dp)) {
            Icon(Icons.Filled.Remove, stringResource(R.string.a11y_decrease_quantity), tint = PaanColors.ForestGreen)
        }
        Text(
            quantity.toString(),
            Modifier.widthIn(min = 24.dp),
            style = MaterialTheme.typography.titleMedium,
            color = PaanColors.DarkBrown,
            textAlign = TextAlign.Center,
        )
        IconButton(onClick = onIncrement, Modifier.size(40.dp)) {
            Icon(Icons.Filled.Add, stringResource(R.string.a11y_increase_quantity), tint = PaanColors.ForestGreen)
        }
    }
}
