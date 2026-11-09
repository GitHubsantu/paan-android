package com.paan.safa.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.paan.safa.ui.theme.PaanColors

private val GrayFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })

@Composable
fun ProductImage(
    url: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    grayscale: Boolean = false,
) {
    if (url == null) {
        LeafPlaceholder(modifier)
    } else {
        SubcomposeAsyncImage(
            model = url,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            colorFilter = if (grayscale) GrayFilter else null,
            modifier = modifier,
            loading = { LeafPlaceholder(Modifier.fillMaxSize()) },
            error = { LeafPlaceholder(Modifier.fillMaxSize()) },
        )
    }
}

@Composable
private fun LeafPlaceholder(modifier: Modifier) {
    Box(modifier.background(PaanColors.ForestGreen.copy(alpha = 0.08f)), contentAlignment = Alignment.Center) {
        PaanLeaf(Modifier.padding(20.dp).fillMaxSize(), color = PaanColors.ForestGreen.copy(alpha = 0.35f))
    }
}