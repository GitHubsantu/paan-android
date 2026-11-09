package com.paan.safa.ui.screens.home

import com.paan.safa.core.ui.asString
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import com.paan.safa.R
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.paan.safa.core.util.AppConfig
import com.paan.safa.core.util.asRupees
import com.paan.safa.domain.model.Order
import com.paan.safa.domain.model.OrderStatus
import com.paan.safa.domain.model.Product
import com.paan.safa.ui.components.*
import com.paan.safa.ui.theme.Dimens
import com.paan.safa.ui.theme.PaanColors
import com.paan.safa.ui.theme.PaanTextStyles
import com.paan.safa.ui.theme.PillShape
import com.paan.safa.core.util.asBadgeCount
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.paan.safa.domain.model.ShopInfo
import com.paan.safa.domain.model.displayName
import com.paan.safa.domain.model.isClosed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

@Composable
fun HomeScreen(
    onTabSelected: (BottomTab) -> Unit,
    onProductClick: (String) -> Unit = {},
    onOrderClick: (String) -> Unit = {},
    onNotifications: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    NotificationPermissionPrompt(stringResource(R.string.home_permission_body))
    val state by viewModel.state.collectAsStateWithLifecycle()
    val unread by viewModel.unreadCount.collectAsStateWithLifecycle()
    val shop by viewModel.shop.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = PaanColors.Cream,
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            PaanBottomBar(BottomTab.Home, state.cartCount, onSelected = onTabSelected)
        },
    ) { padding ->
        PaanBackground(Modifier.padding(padding)) {
            Column(Modifier.fillMaxSize()) {
                HomeTopBar(unreadCount = unread, shop = shop, onNotificationsClick = onNotifications)
                when {
                    state.isLoading -> HomeSkeleton()
                    state.error != null -> ErrorContent(state.error!!.asString(), viewModel::load)
                    else -> HomeContent(
                        state = state,
                        vm = viewModel,
                        shop = shop,
                        onProductClick = onProductClick,
                        onOrderClick = onOrderClick,
                        onCartClick = { onTabSelected(BottomTab.Cart) },
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeTopBar(unreadCount: Int, shop: ShopInfo?, onNotificationsClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(PaanColors.DeepForest)
            .statusBarsPadding()
            .heightIn(min = Dimens.TopBarHeight)
            .padding(horizontal = Dimens.ScreenMargin, vertical = Dimens.SpaceSm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PaanLogo(logoSize = 36.dp)
        Spacer(Modifier.width(Dimens.SpaceSm))
        Column(Modifier.weight(1f)) {
            Text(AppConfig.BRAND_NAME, style = MaterialTheme.typography.titleLarge, color = PaanColors.Cream)
            ShopStatusLine(shop)
        }
        val label = if (unreadCount > 0) stringResource(R.string.home_notifications_unread, unreadCount) else stringResource(R.string.home_notifications)
        IconButton(
            onClick = onNotificationsClick,
            modifier = Modifier.semantics { contentDescription = label },
        ) {
            BadgedBox(
                badge = {
                    if (unreadCount > 0) {
                        Badge(containerColor = PaanColors.RoyalGold, contentColor = PaanColors.DarkBrown) {
                            Text(unreadCount.asBadgeCount())
                        }
                    }
                },
            ) {
                Icon(Icons.Filled.Notifications, contentDescription = null, tint = PaanColors.Cream)
            }
        }
    }
}

@Composable
private fun HomeContent(
    state: HomeUiState,
    vm: HomeViewModel,
    shop: ShopInfo?,
    onProductClick: (String) -> Unit,
    onOrderClick: (String) -> Unit,
    onCartClick: () -> Unit,
) {
    val closed = shop.isClosed
    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(
                start = Dimens.ScreenMargin, end = Dimens.ScreenMargin,
                top = Dimens.SpaceMd, bottom = if (state.cartCount > 0) 96.dp else Dimens.SpaceLg,
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
        ) {
            if (state.activeOrders.isNotEmpty()) {
                item(key = "active-orders", contentType = "active-orders") {
                    ActiveOrdersPager(state.activeOrders, onOrderClick)
                }
            }
            if (closed) {
                item(key = "shop-closed", contentType = "shop-closed") { ShopClosedBanner(shop) }
            }
            item(key = "header", contentType = "header") {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
                    Text(stringResource(R.string.home_welcome), style = MaterialTheme.typography.headlineMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Schedule, null, Modifier.size(16.dp), tint = PaanColors.FreshGreen)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            stringResource(R.string.home_express_prompt),
                            style = MaterialTheme.typography.bodyMedium,
                            color = PaanColors.MutedBrown,
                        )
                    }
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = vm::onQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text(stringResource(R.string.home_search_placeholder)) },
                        leadingIcon = { Icon(Icons.Filled.Search, null) },
                        shape = PillShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = PaanColors.CardWhite,
                            unfocusedContainerColor = PaanColors.CardWhite,
                            focusedBorderColor = PaanColors.ForestGreen,
                            cursorColor = PaanColors.ForestGreen,
                        ),
                    )
                }
            }

            item(key = "chips", contentType = "chips") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item(key = "all", contentType = "chip") {
                        CategoryChip(stringResource(R.string.home_category_all), state.selectedCategoryId == null) { vm.onCategorySelected(null) }
                    }
                    items(state.categories, key = { it.id }, contentType = { "chip" }) { c ->
                        CategoryChip(c.name, state.selectedCategoryId == c.id) { vm.onCategorySelected(c.id) }
                    }
                }
            }

            if (state.featured.isNotEmpty()) {
                item(key = "featured", contentType = "featured") {
                    FeaturedPager(
                        products = state.featured,
                        closed = closed,
                        onProductClick = onProductClick,
                        onOrder = { vm.add(it) },
                    )
                }
            }

            item(key = "menu-title", contentType = "title") {
                Text(stringResource(R.string.home_fresh_rolled), style = MaterialTheme.typography.titleLarge)
            }

            if (state.products.isEmpty()) {
                item(key = "empty", contentType = "empty") {
                    Text(
                        stringResource(R.string.home_no_results),
                        modifier = Modifier.fillMaxWidth().padding(vertical = Dimens.SpaceLg),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = PaanColors.MutedBrown,
                    )
                }
            }

            items(state.products, key = { it.id }, contentType = { "product" }) { p ->
                PaanProductCard(
                    product = p,
                    shopClosed = closed,
                    quantity = state.quantities[p.id] ?: 0,
                    onClick = { onProductClick(p.id) },
                    onAdd = { vm.add(p) },
                    onIncrement = { vm.increment(p) },
                    onDecrement = { vm.decrement(p) },
                )
            }
        }

        if (state.cartCount > 0) {
            CartTray(
                count = state.cartCount,
                total = state.cartTotal,
                closed = closed,
                onClick = onCartClick,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = Dimens.ScreenMargin, vertical = Dimens.SpaceSm),
            )
        }
    }
}

@Composable
private fun ActiveOrdersPager(orders: List<Order>, onOrderClick: (String) -> Unit) {
    val pagerState = rememberPagerState(pageCount = { orders.size })

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HorizontalPager(
            state = pagerState,
            // With several orders, leave space on the right so the next card peeks in.
            contentPadding = PaddingValues(end = if (orders.size > 1) 36.dp else 0.dp),
            pageSpacing = 12.dp,
            key = { index -> orders[index].id },
        ) { page ->
            val order = orders[page]
            ActiveOrderCard(order = order, onClick = { onOrderClick(order.id) })
        }

        if (orders.size > 1) {
            val pageDescription = stringResource(R.string.a11y_order_page, pagerState.currentPage + 1, orders.size)
            Row(
                Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = pageDescription },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(orders.size) { index ->
                    val selected = index == pagerState.currentPage
                    Box(
                        Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (selected) 8.dp else 6.dp)
                            .background(
                                if (selected) PaanColors.ForestGreen else PaanColors.MutedBrown.copy(alpha = 0.35f),
                                CircleShape,
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveOrderCard(order: Order, onClick: () -> Unit) {
    val ready = order.status == OrderStatus.READY
    val container = if (ready) PaanColors.RoyalGold else PaanColors.DeepForest
    val onContainer = if (ready) PaanColors.DarkBrown else PaanColors.Cream

    Surface(
        modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).clickable(onClick = onClick),
        color = container,
        shape = MaterialTheme.shapes.large,
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(stringResource(R.string.home_active_order, order.orderNumber), style = MaterialTheme.typography.titleMedium, color = onContainer)
                Text(
                    if (ready) stringResource(R.string.home_ready_token, order.token)
                    else stringResource(R.string.home_status_token, stringResource(order.status.labelRes), order.token),
                    style = MaterialTheme.typography.bodyMedium,
                    color = onContainer,
                )
                Text(
                    stringResource(R.string.home_order_total, pluralStringResource(R.plurals.paans, order.itemCount, order.itemCount), order.total.asRupees()),
                    style = MaterialTheme.typography.labelSmall,
                    color = onContainer.copy(alpha = 0.8f),
                )
            }
            Text(stringResource(R.string.home_track), style = MaterialTheme.typography.labelLarge, color = onContainer)
            Spacer(Modifier.width(4.dp))
            Icon(Icons.Filled.ArrowForward, contentDescription = null, tint = onContainer)
        }
    }
}

@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelLarge) },
        leadingIcon = if (selected) {
            { Icon(Icons.Filled.Check, null, Modifier.size(16.dp)) }
        } else null,
        shape = MaterialTheme.shapes.small,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = PaanColors.CardWhite,
            labelColor = PaanColors.DarkBrown,
            selectedContainerColor = PaanColors.ForestGreen.copy(alpha = 0.12f),
            selectedLabelColor = PaanColors.ForestGreen,
            selectedLeadingIconColor = PaanColors.ForestGreen,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = PaanColors.MutedBrown.copy(alpha = 0.25f),
            selectedBorderColor = PaanColors.ForestGreen,
            selectedBorderWidth = 1.5.dp,
        ),
    )
}

@Composable
private fun FeaturedPager(
    products: List<Product>,
    closed: Boolean,
    onProductClick: (String) -> Unit,
    onOrder: (Product) -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { products.size })

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(end = if (products.size > 1) 36.dp else 0.dp),
            pageSpacing = 12.dp,
            verticalAlignment = Alignment.Top,
            key = { index -> products[index].id },
        ) { page ->
            val p = products[page]
            FeaturedCard(
                product = p,
                closed = closed,
                onClick = { onProductClick(p.id) },
                onOrder = { onOrder(p) },
            )
        }

        if (products.size > 1) {
            val pageDescription = stringResource(R.string.a11y_featured_page, pagerState.currentPage + 1, products.size)
            Row(
                Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = pageDescription },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(products.size) { index ->
                    val selected = index == pagerState.currentPage
                    Box(
                        Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (selected) 8.dp else 6.dp)
                            .background(
                                if (selected) PaanColors.ForestGreen else PaanColors.MutedBrown.copy(alpha = 0.35f),
                                CircleShape,
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun FeaturedCard(product: Product, closed: Boolean, onClick: () -> Unit, onOrder: () -> Unit) {
    Card(
        Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = PaanColors.DeepForest),
        border = BorderStroke(1.dp, PaanColors.RoyalGold.copy(alpha = 0.5f)),
    ) {
        Box {
            ProductImage(product.imageUrl, product.name, Modifier.fillMaxWidth().height(168.dp), grayscale = closed)
            Text(
                product.tag ?: stringResource(R.string.home_featured),
                Modifier
                    .padding(12.dp)
                    .background(PaanColors.RoyalGold, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                color = PaanColors.DarkBrown,
            )
        }
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(product.name, style = MaterialTheme.typography.titleLarge, color = PaanColors.Cream)
            Text(
                product.description,
                style = MaterialTheme.typography.bodySmall,
                color = PaanColors.LightGold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(product.price.asRupees(), style = PaanTextStyles.Currency, color = PaanColors.LightGold)
                Button(
                    onClick = onOrder,
                    enabled = !closed,
                    shape = PillShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PaanColors.RoyalGold,
                        contentColor = PaanColors.DarkBrown,
                        disabledContainerColor = PaanColors.MutedBrown.copy(alpha = 0.35f),
                        disabledContentColor = PaanColors.Cream,
                    ),
                ) { Text(stringResource(if (closed) R.string.common_closed else R.string.common_order)) }
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                Icon(Icons.Filled.Bolt, null, Modifier.size(14.dp), tint = PaanColors.LightGold)
                Spacer(Modifier.width(4.dp))
                Text(
                    stringResource(R.string.home_freshly_rolled),
                    style = MaterialTheme.typography.labelSmall,
                    color = PaanColors.Cream,
                )
            }
        }
    }
}

@Composable
private fun CartTray(count: Int, total: Int, closed: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).clickable(onClick = onClick),
        color = PaanColors.DeepForest,
        shape = MaterialTheme.shapes.large,
        shadowElevation = 8.dp,
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.home_current_order, total.asRupees()),
                    style = MaterialTheme.typography.titleMedium,
                    color = PaanColors.Cream,
                )
                Text(
                    if (closed) stringResource(R.string.home_closed_paused)
                    else stringResource(R.string.home_cart_ready, pluralStringResource(R.plurals.items, count, count)),
                    style = MaterialTheme.typography.labelSmall,
                    color = PaanColors.LightGold,
                )
            }
            Icon(Icons.Filled.ArrowForward, stringResource(R.string.a11y_view_cart), tint = PaanColors.LightGold)
        }
    }
}

private val OpenDot = Color(0xFF3DDC84)
private val ClosedDot = Color(0xFFFF6B6B)

@Composable
private fun ShopStatusLine(shop: ShopInfo?) {
    val shopDescription = when {
        shop == null -> shop.displayName
        shop.isOpen -> stringResource(R.string.home_shop_open_a11y, shop.displayName)
        else -> stringResource(R.string.home_shop_closed_a11y, shop.displayName)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.semantics(mergeDescendants = true) {
            contentDescription = shopDescription
        },
    ) {
        if (shop != null) {
            Box(Modifier.size(8.dp).background(if (shop.isOpen) OpenDot else ClosedDot, CircleShape))
            Spacer(Modifier.width(6.dp))
            Text(
                stringResource(if (shop.isOpen) R.string.common_open else R.string.common_closed),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = PaanColors.Cream,
            )
            Text(stringResource(R.string.common_separator), style = MaterialTheme.typography.labelSmall, color = PaanColors.LightGold)
        }
        Text(shop.displayName, style = MaterialTheme.typography.labelSmall, color = PaanColors.LightGold)
    }
}

@Composable
private fun ShopClosedBanner(shop: ShopInfo?) {
    Card(
        Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = PaanColors.CardWhite),
        border = BorderStroke(1.dp, PaanColors.Maroon.copy(alpha = 0.5f)),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Storefront, null, tint = PaanColors.Maroon)
            Spacer(Modifier.width(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(stringResource(R.string.home_closed_now), style = MaterialTheme.typography.titleMedium, color = PaanColors.Maroon)
                Text(
                    stringResource(R.string.home_closed_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = PaanColors.MutedBrown,
                )
                shop?.hours?.let {
                    Text(stringResource(R.string.home_usually_open, it), style = MaterialTheme.typography.labelSmall, color = PaanColors.MutedBrown)
                }
            }
        }
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(Dimens.SpaceLg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(stringResource(R.string.home_menu_load_error), style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(Dimens.SpaceSm))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = PaanColors.MutedBrown)
        Spacer(Modifier.height(Dimens.SpaceMd))
        PrimaryPaanButton(stringResource(R.string.common_try_again), onRetry)
    }
}
