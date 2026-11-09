package com.paan.safa.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
fun NotificationsSkeleton(modifier: Modifier = Modifier) {
    val p = rememberShimmerProgress()
    val loadingDescription = stringResource(R.string.a11y_loading_notifications)
    Column(
        modifier
            .fillMaxSize()
            .padding(Dimens.ScreenMargin)
            .clearAndSetSemantics { contentDescription = loadingDescription },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        repeat(5) { SkeletonBox(p, Modifier.fillMaxWidth().height(96.dp), RoundedCornerShape(16.dp)) }
    }
}
