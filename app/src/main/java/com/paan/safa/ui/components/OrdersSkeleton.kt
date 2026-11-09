package com.paan.safa.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.paan.safa.R
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.paan.safa.ui.theme.Dimens

@Composable
fun OrdersSkeleton(modifier: Modifier = Modifier) {
    val p = rememberShimmerProgress()
    val loadingDescription = stringResource(R.string.a11y_loading_orders)
    Column(
        modifier
            .fillMaxSize()
            .padding(Dimens.ScreenMargin)
            .clearAndSetSemantics { contentDescription = loadingDescription },
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
    ) {
        repeat(3) { SkeletonBox(p, Modifier.fillMaxWidth().height(150.dp), RoundedCornerShape(16.dp)) }
    }
}
