package com.paan.safa.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.annotation.StringRes
import com.paan.safa.R
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paan.safa.domain.model.OrderStatus
import com.paan.safa.ui.theme.PaanColors

private data class TimelineStep(@StringRes val titleRes: Int, @StringRes val noteRes: Int)

private val Steps = listOf(
    TimelineStep(R.string.timeline_order_placed, R.string.timeline_order_placed_note),
    TimelineStep(R.string.timeline_preparing, R.string.timeline_preparing_note),
    TimelineStep(R.string.timeline_ready, R.string.timeline_ready_note),
    TimelineStep(R.string.timeline_completed, R.string.timeline_completed_note),
)

private enum class StepState { Done, Active, Pending }

/** Shows ✓ Order Placed → ● Being Prepared → ○ Ready → ○ Completed. Not used for cancelled orders. */
@Composable
fun OrderStatusTimeline(status: OrderStatus, modifier: Modifier = Modifier) {
    // index of the step the order is currently on; 4 = everything finished
    val activeIndex = when (status) {
        OrderStatus.PENDING, OrderStatus.MAKING -> 1
        OrderStatus.READY -> 2
        OrderStatus.COMPLETED -> 4
        OrderStatus.ADMIN_CANCELLED, OrderStatus.PAYMENT_FAILED -> return
    }

    Column(modifier) {
        Steps.forEachIndexed { index, step ->
            val state = when {
                index < activeIndex -> StepState.Done
                index == activeIndex -> StepState.Active
                else -> StepState.Pending
            }
            val label = when (state) {
                StepState.Done -> stringResource(R.string.timeline_done)
                StepState.Active -> stringResource(if (index == 2) R.string.timeline_ready_now else R.string.timeline_in_progress)
                StepState.Pending -> stringResource(R.string.timeline_pending)
            }
            val isLast = index == Steps.lastIndex

            Row(Modifier.height(IntrinsicSize.Min)) {
                Column(Modifier.width(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    StepIndicator(state)
                    if (!isLast) {
                        Box(
                            Modifier
                                .width(2.dp)
                                .weight(1f)
                                .background(
                                    if (state == StepState.Done) PaanColors.ForestGreen
                                    else PaanColors.MutedBrown.copy(alpha = 0.3f),
                                ),
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.padding(bottom = if (isLast) 0.dp else 20.dp)) {
                    Text(
                        stringResource(step.titleRes),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (state == StepState.Pending) PaanColors.MutedBrown else PaanColors.DarkBrown,
                    )
                    Text(
                        label,
                        style = MaterialTheme.typography.labelMedium,
                        color = when (state) {
                            StepState.Done -> PaanColors.FreshGreen
                            StepState.Active -> PaanColors.ForestGreen
                            StepState.Pending -> PaanColors.MutedBrown
                        },
                    )
                    Text(stringResource(step.noteRes), style = MaterialTheme.typography.bodySmall, color = PaanColors.MutedBrown)
                }
            }
        }
    }
}

@Composable
private fun StepIndicator(state: StepState) {
    when (state) {
        StepState.Done -> Box(
            Modifier.size(28.dp).background(PaanColors.ForestGreen, CircleShape),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Filled.Check, contentDescription = stringResource(R.string.timeline_done), tint = PaanColors.Cream, modifier = Modifier.size(16.dp)) }

        StepState.Active -> Box(
            Modifier.size(28.dp).border(2.dp, PaanColors.ForestGreen, CircleShape),
            contentAlignment = Alignment.Center,
        ) { Box(Modifier.size(12.dp).background(PaanColors.ForestGreen, CircleShape)) }

        StepState.Pending -> Box(
            Modifier.size(28.dp).border(2.dp, PaanColors.MutedBrown.copy(alpha = 0.4f), CircleShape),
        )
    }
}
