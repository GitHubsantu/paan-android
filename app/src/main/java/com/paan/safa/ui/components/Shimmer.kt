package com.paan.safa.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

private val ShimmerBase = Color(0x1F806B5A)        // soft brown, ~12%
private val ShimmerHighlight = Color(0x99FFFFFF)   // white sweep

/** One animation shared by every skeleton block on a screen. */
@Composable
fun rememberShimmerProgress(): State<Float> {
    val transition = rememberInfiniteTransition(label = "shimmer")
    return transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmerProgress",
    )
}

/**
 * The progress value is read inside the draw block, so the animation only redraws.
 * It never recomposes the screen.
 */
fun Modifier.shimmer(progress: State<Float>, shape: Shape = RoundedCornerShape(8.dp)): Modifier =
    this
        .clip(shape)
        .background(ShimmerBase)
        .drawWithContent {
            drawContent()
            val band = size.width * 0.7f
            val x = -band + (size.width + band) * progress.value
            drawRect(
                Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, ShimmerHighlight, Color.Transparent),
                    startX = x,
                    endX = x + band,
                ),
            )
        }

@Composable
fun SkeletonBox(
    progress: State<Float>,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp),
) {
    Box(modifier.shimmer(progress, shape))
}