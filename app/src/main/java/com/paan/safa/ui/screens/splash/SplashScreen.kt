package com.paan.safa.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.paan.safa.R
import com.paan.safa.ui.components.GoldDivider
import com.paan.safa.ui.components.PaanBackground
import com.paan.safa.ui.components.PaanLogo
import com.paan.safa.ui.theme.PaanColors
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.85f) }
    val finished by rememberUpdatedState(onFinished)

    LaunchedEffect(Unit) {
        coroutineScope {
            launch { alpha.animateTo(1f, tween(700)) }
            launch { scale.animateTo(1f, tween(700, easing = FastOutSlowInEasing)) }
        }
        delay(1000)
        finished()
    }

    PaanBackground(
        background = Brush.verticalGradient(listOf(PaanColors.DeepForest, PaanColors.ForestGreen)),
        leafColor = PaanColors.LightGold.copy(alpha = 0.07f),
        veinColor = PaanColors.LightGold.copy(alpha = 0.10f),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .graphicsLayer {
                    this.alpha = alpha.value
                    scaleX = scale.value
                    scaleY = scale.value
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            PaanLogo(logoSize = 112.dp)
            Spacer(Modifier.height(24.dp))
            Text(stringResource(R.string.splash_title), style = MaterialTheme.typography.displayMedium, color = PaanColors.Cream)
            Spacer(Modifier.height(12.dp))
            GoldDivider()
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(R.string.splash_credit),
                style = MaterialTheme.typography.bodyMedium,
                color = PaanColors.LightGold,
            )
        }
    }
}
