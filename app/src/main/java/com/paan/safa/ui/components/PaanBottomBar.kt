package com.paan.safa.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.annotation.StringRes
import com.paan.safa.R
import com.paan.safa.ui.theme.PaanColors

enum class BottomTab(@StringRes val labelRes: Int, val icon: ImageVector) {
    Home(R.string.nav_home, Icons.Filled.Spa),
    Orders(R.string.nav_orders, Icons.Filled.ConfirmationNumber),
    Cart(R.string.nav_cart, Icons.Filled.ShoppingBag),
    Profile(R.string.nav_profile, Icons.Filled.Person),
}

@Composable
fun PaanBottomBar(
    selected: BottomTab,
    cartCount: Int,
    onSelected: (BottomTab) -> Unit,
) {
    NavigationBar(containerColor = PaanColors.CardWhite) {
        BottomTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = tab == selected,
                onClick = { onSelected(tab) },
                label = { Text(stringResource(tab.labelRes)) },
                icon = {
                    BadgedBox(badge = {
                        if (tab == BottomTab.Cart && cartCount > 0) {
                            Badge(containerColor = PaanColors.Maroon) { Text(cartCount.toString()) }
                        }
                    }) { Icon(tab.icon, contentDescription = stringResource(tab.labelRes)) }
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = PaanColors.ForestGreen,
                    selectedIconColor = PaanColors.LightGold,
                    selectedTextColor = PaanColors.ForestGreen,
                    unselectedIconColor = PaanColors.MutedBrown,
                    unselectedTextColor = PaanColors.MutedBrown,
                ),
            )
        }
    }
}
