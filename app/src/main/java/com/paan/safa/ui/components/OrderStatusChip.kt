package com.paan.safa.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.paan.safa.domain.model.OrderStatus
import com.paan.safa.ui.theme.PaanColors
import com.paan.safa.ui.theme.PillShape

/** Status shown as icon + text (never colour alone). */
@Composable
fun OrderStatusChip(status: OrderStatus, modifier: Modifier = Modifier) {
    val (icon, bg, fg) = when (status) {
        OrderStatus.PENDING -> Triple(Icons.Filled.Schedule, PaanColors.RoyalGold.copy(alpha = 0.2f), PaanColors.DarkBrown)
        OrderStatus.MAKING -> Triple(Icons.Filled.Timelapse, PaanColors.ForestGreen.copy(alpha = 0.12f), PaanColors.ForestGreen)
        OrderStatus.READY -> Triple(Icons.Filled.NotificationsActive, PaanColors.RoyalGold, PaanColors.DarkBrown)
        OrderStatus.COMPLETED -> Triple(Icons.Filled.DoneAll, PaanColors.FreshGreen.copy(alpha = 0.15f), PaanColors.FreshGreen)
        OrderStatus.ADMIN_CANCELLED -> Triple(Icons.Filled.Cancel, PaanColors.Maroon.copy(alpha = 0.12f), PaanColors.Maroon)
        OrderStatus.PAYMENT_FAILED -> Triple(Icons.Filled.Cancel, PaanColors.Maroon.copy(alpha = 0.12f), PaanColors.Maroon)
    }
    Surface(modifier = modifier, shape = PillShape, color = bg) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(stringResource(status.labelRes), style = MaterialTheme.typography.labelMedium, color = fg)
        }
    }
}
