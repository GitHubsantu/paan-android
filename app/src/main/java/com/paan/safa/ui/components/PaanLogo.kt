package com.paan.safa.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.paan.safa.ui.theme.PaanColors

/** Dual-leaf brand insignia inside a gold ring. */
@Composable
fun PaanLogo(modifier: Modifier = Modifier, logoSize: Dp = 96.dp) {
    Box(
        modifier
            .size(logoSize)
            .clip(CircleShape)
            .background(PaanColors.DeepForest)
            .border(2.dp, PaanColors.RoyalGold, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val s = this.size.width * 0.5f
            val c = this.size.width / 2
            drawPaanLeaf(Offset(c - s * 0.85f, c - s * 0.5f), s, -25f, PaanColors.FreshGreen, PaanColors.LightGold)
            drawPaanLeaf(Offset(c - s * 0.15f, c - s * 0.5f), s, 25f, PaanColors.ForestGreen, PaanColors.LightGold)
        }
    }
}