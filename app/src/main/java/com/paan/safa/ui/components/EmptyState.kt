package com.paan.safa.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.paan.safa.ui.theme.Dimens
import com.paan.safa.ui.theme.PaanColors

@Composable
fun EmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier.fillMaxSize().padding(Dimens.SpaceLg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        PaanLeaf(Modifier.size(96.dp), color = PaanColors.ForestGreen.copy(alpha = 0.25f))
        Spacer(Modifier.height(Dimens.SpaceMd))
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(Dimens.SpaceSm))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = PaanColors.MutedBrown,
            textAlign = TextAlign.Center,
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(Dimens.SpaceLg))
            PrimaryPaanButton(actionLabel, onAction)
        }
    }
}