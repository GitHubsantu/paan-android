package com.paan.safa.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import com.paan.safa.ui.theme.PaanColors

/** Screen background with a subtle botanical leaf pattern behind [content]. */
@Composable
fun PaanBackground(
    modifier: Modifier = Modifier,
    background: Brush = SolidColor(PaanColors.Cream),
    leafColor: Color = PaanColors.ForestGreen.copy(alpha = 0.06f),
    veinColor: Color = PaanColors.RoyalGold.copy(alpha = 0.12f),
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier.fillMaxSize().background(background)) {
        Canvas(Modifier.fillMaxSize()) {
            val big = size.width * 0.6f
            val small = size.width * 0.35f
            drawPaanLeaf(Offset(-big * 0.25f, -big * 0.2f), big, -30f, leafColor, veinColor)
            drawPaanLeaf(Offset(size.width - big * 0.75f, size.height - big * 0.8f), big, 150f, leafColor, veinColor)
            drawPaanLeaf(Offset(size.width - small * 0.6f, size.height * 0.35f), small, 60f, leafColor, veinColor)
        }
        content()
    }
}