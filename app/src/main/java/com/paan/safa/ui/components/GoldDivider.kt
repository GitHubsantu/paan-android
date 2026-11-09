package com.paan.safa.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.paan.safa.ui.theme.PaanColors

/** Thin gold line with a centre diamond, used as an ornamental separator. */
@Composable
fun GoldDivider(modifier: Modifier = Modifier) {
    val gold = PaanColors.RoyalGold
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        HorizontalDivider(Modifier.width(48.dp), color = gold.copy(alpha = 0.6f))
        Canvas(Modifier.padding(horizontal = 8.dp).size(8.dp)) {
            rotate(45f) { drawRect(gold) }
        }
        HorizontalDivider(Modifier.width(48.dp), color = gold.copy(alpha = 0.6f))
    }
}