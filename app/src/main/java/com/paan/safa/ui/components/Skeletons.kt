package com.paan.safa.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.paan.safa.R
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.paan.safa.ui.theme.Dimens
import com.paan.safa.ui.theme.PaanColors
import com.paan.safa.ui.theme.PillShape

private val CardShape = RoundedCornerShape(16.dp)

@Composable
fun HomeSkeleton(modifier: Modifier = Modifier) {
    val p = rememberShimmerProgress()
    val loadingDescription = stringResource(R.string.a11y_loading_menu)
    Column(
        modifier
            .fillMaxSize()
            .padding(horizontal = Dimens.ScreenMargin, vertical = Dimens.SpaceMd)
            .clearAndSetSemantics { contentDescription = loadingDescription },
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
    ) {
        SkeletonBox(p, Modifier.width(150.dp).height(28.dp))
        SkeletonBox(p, Modifier.fillMaxWidth(0.8f).height(14.dp))
        SkeletonBox(p, Modifier.fillMaxWidth().height(52.dp), PillShape)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SkeletonBox(p, Modifier.width(56.dp).height(36.dp))
            SkeletonBox(p, Modifier.width(150.dp).height(36.dp))
            SkeletonBox(p, Modifier.width(120.dp).height(36.dp))
        }
        SkeletonBox(p, Modifier.fillMaxWidth().height(200.dp), CardShape)
        repeat(3) { ProductCardSkeleton(p) }
    }
}

@Composable
private fun ProductCardSkeleton(p: State<Float>) {
    Card(
        Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = PaanColors.CardWhite),
        border = BorderStroke(1.dp, PaanColors.RoyalGold.copy(alpha = 0.2f)),
    ) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SkeletonBox(p, Modifier.size(96.dp), RoundedCornerShape(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SkeletonBox(p, Modifier.width(70.dp).height(10.dp))
                SkeletonBox(p, Modifier.fillMaxWidth(0.8f).height(16.dp))
                SkeletonBox(p, Modifier.fillMaxWidth().height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    SkeletonBox(p, Modifier.width(50.dp).height(22.dp))
                    SkeletonBox(p, Modifier.width(72.dp).height(32.dp), PillShape)
                }
            }
        }
    }
}

@Composable
fun ProductDetailSkeleton(modifier: Modifier = Modifier) {
    val p = rememberShimmerProgress()
    val loadingDescription = stringResource(R.string.a11y_loading_product)
    Column(
        modifier.fillMaxSize().clearAndSetSemantics { contentDescription = loadingDescription },
    ) {
        SkeletonBox(p, Modifier.fillMaxWidth().height(300.dp), RoundedCornerShape(0.dp))
        Column(
            Modifier.padding(Dimens.ScreenMargin),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
        ) {
            SkeletonBox(p, Modifier.width(120.dp).height(12.dp))
            SkeletonBox(p, Modifier.fillMaxWidth(0.8f).height(30.dp))
            SkeletonBox(p, Modifier.width(90.dp).height(24.dp))
            SkeletonBox(p, Modifier.fillMaxWidth().height(14.dp))
            SkeletonBox(p, Modifier.fillMaxWidth().height(14.dp))
            SkeletonBox(p, Modifier.fillMaxWidth(0.6f).height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SkeletonBox(p, Modifier.width(110.dp).height(34.dp))
                SkeletonBox(p, Modifier.width(90.dp).height(34.dp))
                SkeletonBox(p, Modifier.width(100.dp).height(34.dp))
            }
            SkeletonBox(p, Modifier.fillMaxWidth().height(72.dp), CardShape)
        }
    }
}

@Composable
fun OrderStatusSkeleton(modifier: Modifier = Modifier) {
    val p = rememberShimmerProgress()
    val loadingDescription = stringResource(R.string.a11y_loading_order)
    Column(
        modifier
            .fillMaxSize()
            .padding(Dimens.ScreenMargin)
            .clearAndSetSemantics { contentDescription = loadingDescription },
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
    ) {
        SkeletonBox(p, Modifier.fillMaxWidth().height(120.dp), CardShape)
        SkeletonBox(p, Modifier.fillMaxWidth().height(120.dp), CardShape)
        SkeletonBox(p, Modifier.fillMaxWidth().height(64.dp), CardShape)
        SkeletonBox(p, Modifier.fillMaxWidth().height(220.dp), CardShape)
    }
}
