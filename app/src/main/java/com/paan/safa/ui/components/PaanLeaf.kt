package com.paan.safa.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import com.paan.safa.ui.theme.PaanColors

/** Betel leaf outline in a 1×1 box. Built once and reused, so drawing never allocates. */
private val UnitLeaf: Path = Path().apply {
    moveTo(0.5f, 0f)
    cubicTo(0.72f, 0.18f, 1.0f, 0.45f, 0.92f, 0.72f)
    cubicTo(0.86f, 0.92f, 0.64f, 0.98f, 0.5f, 0.86f)
    cubicTo(0.36f, 0.98f, 0.14f, 0.92f, 0.08f, 0.72f)
    cubicTo(0.0f, 0.45f, 0.28f, 0.18f, 0.5f, 0f)
    close()
}

// (y of the vein's outer end, horizontal reach)
private val SideVeins = listOf(0.30f to 0.16f, 0.46f to 0.27f, 0.62f to 0.32f)

/** Draws a betel (paan) leaf. [s] is the square box size in pixels. */
fun DrawScope.drawPaanLeaf(
    topLeft: Offset,
    s: Float,
    rotation: Float,
    fill: Color,
    vein: Color,
) {
    withTransform({
        translate(topLeft.x, topLeft.y)
        rotate(rotation, Offset(s / 2, s / 2))
        scale(s, s, Offset.Zero)           // everything below is in 0..1 units
    }) {
        drawPath(UnitLeaf, fill)
        drawLine(fill, Offset(0.5f, 0.86f), Offset(0.5f, 1.0f), strokeWidth = 0.04f, cap = StrokeCap.Round)
        drawLine(vein, Offset(0.5f, 0.06f), Offset(0.5f, 0.86f), strokeWidth = 0.025f, cap = StrokeCap.Round)
        for ((y, dx) in SideVeins) {
            val start = Offset(0.5f, y + 0.12f)
            drawLine(vein, start, Offset(0.5f - dx, y), strokeWidth = 0.018f, cap = StrokeCap.Round)
            drawLine(vein, start, Offset(0.5f + dx, y), strokeWidth = 0.018f, cap = StrokeCap.Round)
        }
    }
}

@Composable
fun PaanLeaf(
    modifier: Modifier = Modifier,
    color: Color = PaanColors.ForestGreen,
    veinColor: Color = PaanColors.LightGold.copy(alpha = 0.7f),
    rotation: Float = 0f,
) {
    Canvas(modifier.aspectRatio(1f)) {
        drawPaanLeaf(Offset.Zero, size.minDimension, rotation, color, veinColor)
    }
}