package com.paan.safa.ui.screens.notifications

import com.paan.safa.core.ui.asString
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.paan.safa.R
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.paan.safa.core.util.asTimeAgo
import com.paan.safa.domain.model.AppNotification
import com.paan.safa.ui.components.EmptyState
import com.paan.safa.ui.components.NotificationsSkeleton
import com.paan.safa.ui.components.OrderStatusChip
import com.paan.safa.ui.components.PaanBackground
import com.paan.safa.ui.theme.Dimens
import com.paan.safa.ui.theme.PaanColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    onOpenOrder: (String) -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }

    val messageText = state.message?.asString()
    LaunchedEffect(messageText) {
        messageText?.let {
            snackbar.showSnackbar(it)
            viewModel.messageShown()
        }
    }

    Scaffold(
        containerColor = PaanColors.Cream,
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbar, Modifier.navigationBarsPadding()) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.notifications_title), style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.a11y_back)) }
                },
                actions = {
                    if (state.unreadCount > 0) {
                        TextButton(onClick = viewModel::markAllRead) {
                            Text(stringResource(R.string.notifications_mark_all_read), color = PaanColors.ForestGreen)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PaanColors.Cream),
            )
        },
    ) { padding ->
        PaanBackground(Modifier.padding(padding)) {
            when {
                state.isLoading -> NotificationsSkeleton()
                state.error != null -> EmptyState(
                    title = stringResource(R.string.notifications_load_error_title),
                    message = state.error!!.asString(),
                    actionLabel = stringResource(R.string.common_try_again),
                    onAction = viewModel::load,
                )
                state.items.isEmpty() -> EmptyState(
                    title = stringResource(R.string.notifications_empty_title),
                    message = stringResource(R.string.notifications_empty_body),
                )
                else -> NotificationList(
                    items = state.items,
                    hasMore = state.hasMore,
                    isLoadingMore = state.isLoadingMore,
                    onLoadMore = viewModel::loadMore,
                    onClick = { n ->
                        viewModel.onOpen(n)
                        n.orderId?.let(onOpenOrder)
                    },
                    onDelete = viewModel::delete,
                )
            }
        }
    }
}

@Composable
private fun NotificationList(
    items: List<AppNotification>,
    hasMore: Boolean,
    isLoadingMore: Boolean,
    onLoadMore: () -> Unit,
    onClick: (AppNotification) -> Unit,
    onDelete: (String) -> Unit,
) {
    val listState = rememberLazyListState()
    val nearEnd by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            last >= info.totalItemsCount - 3
        }
    }
    LaunchedEffect(nearEnd, hasMore, isLoadingMore) {
        if (nearEnd && hasMore && !isLoadingMore) onLoadMore()
    }

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(
            start = Dimens.ScreenMargin,
            end = Dimens.ScreenMargin,
            top = Dimens.SpaceSm,
            bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + Dimens.SpaceMd,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(items, key = { it.id }, contentType = { "notification" }) { n ->
            NotificationItem(
                n = n,
                onClick = { onClick(n) },
                onDelete = { onDelete(n.id) },
                modifier = Modifier.animateItem(),
            )
        }
        if (isLoadingMore) {
            item(key = "loading-more", contentType = "loading") {
                Box(Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(Modifier.size(24.dp), color = PaanColors.ForestGreen, strokeWidth = 2.dp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationItem(
    n: AppNotification,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) { onDelete(); true } else false
        },
    )
    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                Box(
                    Modifier.fillMaxSize().clip(MaterialTheme.shapes.large).background(PaanColors.Maroon),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.a11y_delete),
                        tint = PaanColors.Cream,
                        modifier = Modifier.padding(end = 24.dp),
                    )
                }
            }
        },
    ) {
        NotificationCard(n, onClick, onDelete)
    }
}

@Composable
private fun NotificationCard(n: AppNotification, onClick: () -> Unit, onDelete: () -> Unit) {
    val context = LocalContext.current
    val unread = !n.isRead
    val unreadLabel = stringResource(R.string.a11y_unread)
    var menuOpen by remember { mutableStateOf(false) }

    Card(
        Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = PaanColors.CardWhite),
        border = BorderStroke(1.dp, PaanColors.RoyalGold.copy(alpha = 0.25f)),
    ) {
        Row(Modifier.height(IntrinsicSize.Min).clickable(onClick = onClick)) {
            // Gold left border = unread.
            Box(
                Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(if (unread) PaanColors.RoyalGold else Color.Transparent),
            )
            Column(
                Modifier.weight(1f).padding(start = 12.dp, top = 4.dp, bottom = 12.dp, end = 4.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        n.title,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (unread) FontWeight.Bold else FontWeight.Normal,
                        color = PaanColors.DarkBrown,
                    )
                    if (unread) {
                        Box(
                            Modifier
                                .padding(horizontal = 4.dp)
                                .size(10.dp)
                                .background(PaanColors.ForestGreen, CircleShape)
                                .semantics { contentDescription = unreadLabel },
                        )
                    }
                    Box {
                        IconButton(onClick = { menuOpen = true }) {
                            Icon(Icons.Filled.MoreVert, stringResource(R.string.a11y_more_options), tint = PaanColors.MutedBrown)
                        }
                        DropdownMenu(
                            expanded = menuOpen,
                            onDismissRequest = { menuOpen = false },
                            containerColor = PaanColors.Cream,
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.common_delete)) },
                                leadingIcon = { Icon(Icons.Filled.Delete, null, tint = PaanColors.Maroon) },
                                onClick = { menuOpen = false; onDelete() },
                            )
                        }
                    }
                }
                if (n.body.isNotBlank()) {
                    Text(
                        n.body,
                        modifier = Modifier.padding(end = 12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = PaanColors.MutedBrown,
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth().padding(end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    // Icon + text, never colour alone.
                    n.status?.let { OrderStatusChip(it) } ?: Spacer(Modifier)
                    Text(
                        n.createdAt.asTimeAgo(context),
                        style = MaterialTheme.typography.labelSmall,
                        color = PaanColors.MutedBrown,
                    )
                }
            }
        }
    }
}
