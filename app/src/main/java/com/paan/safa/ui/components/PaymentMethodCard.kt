package com.paan.safa.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.paan.safa.domain.model.PaymentMethod
import com.paan.safa.ui.theme.PaanColors

@Composable
fun PaymentMethodCard(
    method: PaymentMethod,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = if (selected) PaanColors.ForestGreen.copy(alpha = 0.06f) else PaanColors.CardWhite,
        border = BorderStroke(
            if (selected) 2.dp else 1.dp,
            if (selected) PaanColors.ForestGreen else PaanColors.MutedBrown.copy(alpha = 0.25f),
        ),
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = when (method) {
                    PaymentMethod.Upi -> Icons.Filled.QrCode2
                    PaymentMethod.Card -> Icons.Filled.CreditCard
                },
                contentDescription = null,
                tint = PaanColors.ForestGreen,
            )
            Column(Modifier.weight(1f)) {
                Text(stringResource(method.labelRes), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(method.subtitleRes), style = MaterialTheme.typography.bodySmall, color = PaanColors.MutedBrown)
            }
            RadioButton(
                selected = selected,
                onClick = null,
                colors = RadioButtonDefaults.colors(selectedColor = PaanColors.ForestGreen),
            )
        }
    }
}
