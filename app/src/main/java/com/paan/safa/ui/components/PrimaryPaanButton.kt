package com.paan.safa.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paan.safa.ui.theme.Dimens
import com.paan.safa.ui.theme.PaanColors
import com.paan.safa.ui.theme.PillShape

@Composable
fun PrimaryPaanButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = PillShape,
        modifier = modifier.height(Dimens.ButtonHeight),
        colors = ButtonDefaults.buttonColors(
            containerColor = PaanColors.ForestGreen,
            contentColor = PaanColors.Cream,
        ),
    ) {
        if (isLoading) {
            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = PaanColors.Cream)
        } else {
            Text(text, style = androidx.compose.material3.MaterialTheme.typography.labelLarge)
        }
    }
}