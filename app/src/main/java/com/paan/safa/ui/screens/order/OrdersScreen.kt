package com.paan.safa.ui.screens.orders

import com.paan.safa.core.ui.asString
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.paan.safa.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.paan.safa.ui.components.BottomTab
import com.paan.safa.ui.components.EmptyState
import com.paan.safa.ui.components.OrderCard
import com.paan.safa.ui.components.PaanBackground
import androidx.compose.foundation.background
import com.paan.safa.ui.components.PaanBottomBar
import com.paan.safa.ui.components.OrdersSkeleton
import com.paan.safa.ui.theme.Dimens
import com.paan.safa.ui.theme.PaanColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    onTabSelected: (BottomTab) -> Unit,
    onOrderClick: (String) -> Unit,
    viewModel: OrdersViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = PaanColors.Cream,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            Column(Modifier.background(PaanColors.Cream)) {
                TopAppBar(
                    title = { Text(stringResource(R.string.orders_title), style = MaterialTheme.typography.titleLarge) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = PaanColors.Cream),
                )
                PrimaryTabRow(
                    selectedTabIndex = state.tab.ordinal,
                    containerColor = PaanColors.Cream,
                    contentColor = PaanColors.ForestGreen,
                ) {
                    OrdersTab.entries.forEach { tab ->
                        val count = if (tab == OrdersTab.Active) state.active.size else state.past.size
                        Tab(
                            selected = state.tab == tab,
                            onClick = { viewModel.onTabSelected(tab) },
                            text = { Text(stringResource(R.string.orders_tab_count, stringResource(tab.titleRes), count), style = MaterialTheme.typography.labelLarge) },
                            unselectedContentColor = PaanColors.MutedBrown,
                        )
                    }
                }
            }
        },
        bottomBar = {
            PaanBottomBar(BottomTab.Orders, state.cartCount, onSelected = onTabSelected)
        },
    ) { padding ->
        PaanBackground(Modifier.padding(padding)) {
            val shown = state.shown
            when {
                state.isLoading -> OrdersSkeleton()
                state.error != null -> EmptyState(
                    title = stringResource(R.string.orders_load_error_title),
                    message = state.error!!.asString(),
                    actionLabel = stringResource(R.string.common_try_again),
                    onAction = viewModel::retry,
                )
                shown.isEmpty() && state.tab == OrdersTab.Active -> EmptyState(
                    title = stringResource(R.string.orders_empty_active_title),
                    message = stringResource(R.string.orders_empty_active_body),
                    actionLabel = stringResource(R.string.orders_empty_active_action),
                    onAction = { onTabSelected(BottomTab.Home) },
                )
                shown.isEmpty() -> EmptyState(
                    title = stringResource(R.string.orders_empty_past_title),
                    message = stringResource(R.string.orders_empty_past_body),
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(Dimens.ScreenMargin),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
                ) {
                    items(shown, key = { it.id }, contentType = { "order" }) { order ->
                        OrderCard(order = order, onViewDetails = { onOrderClick(order.id) })
                    }
                }
            }
        }
    }
}
