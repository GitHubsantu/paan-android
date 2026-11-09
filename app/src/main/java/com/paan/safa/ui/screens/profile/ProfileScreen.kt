package com.paan.safa.ui.screens.profile

import androidx.compose.ui.res.pluralStringResource
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.paan.safa.R
import com.paan.safa.core.ui.UiText
import com.paan.safa.core.ui.asString
import com.paan.safa.core.util.AppConfig
import com.paan.safa.core.util.asIndianPhone
import com.paan.safa.core.util.asInitials
import com.paan.safa.core.util.openDialer
import com.paan.safa.ui.components.BottomTab
import com.paan.safa.ui.components.GoldDivider
import com.paan.safa.ui.components.PaanBackground
import com.paan.safa.ui.components.PaanBottomBar
import com.paan.safa.ui.components.ProfileMenuItem
import com.paan.safa.ui.theme.Dimens
import com.paan.safa.ui.theme.PaanColors
import com.paan.safa.ui.theme.PaanTextStyles
import com.paan.safa.core.util.asBadgeCount
import com.paan.safa.domain.model.displayName
import com.paan.safa.domain.model.callNumber

@Composable
fun ProfileScreen(
    onTabSelected: (BottomTab) -> Unit,
    onMyOrders: () -> Unit,
    onNotifications: () -> Unit,
    onHelp: () -> Unit,
    onTerms: () -> Unit,
    onPrivacy: () -> Unit,
    onLanguage: () -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showEdit by remember { mutableStateOf(false) }
    var showLogout by remember { mutableStateOf(false) }

    val version = remember {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull().orEmpty()
    }

    Scaffold(
        containerColor = PaanColors.Cream,
        contentWindowInsets = WindowInsets(0),
        bottomBar = { PaanBottomBar(BottomTab.Profile, state.cartCount, onSelected = onTabSelected) },
    ) { padding ->
        PaanBackground(Modifier.padding(padding)) {
            LazyColumn(
                contentPadding = PaddingValues(bottom = Dimens.SpaceLg),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
            ) {
                item {
                    ProfileHeader(
                        name = state.user?.name.orEmpty(),
                        phone = state.user?.phone.orEmpty(),
                        collected = state.collectedCount,
                        onEdit = { showEdit = true },
                    )
                }

                item {
                    Row(
                        Modifier.padding(horizontal = Dimens.ScreenMargin),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
                    ) {
                        StatCard(stringResource(R.string.profile_active), state.activeCount.toString(), Modifier.weight(1f))
                        StatCard(stringResource(R.string.profile_paans_ordered), state.paansOrdered.toString(), Modifier.weight(1f))
                        StatCard(stringResource(R.string.profile_collected), state.collectedCount.toString(), Modifier.weight(1f))
                    }
                }

                item {
                    Card(
                        Modifier.padding(horizontal = Dimens.ScreenMargin).fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = PaanColors.CardWhite),
                        border = BorderStroke(1.dp, PaanColors.RoyalGold.copy(alpha = 0.25f)),
                    ) {
                        Column {
                            ProfileMenuItem(
                                icon = Icons.Filled.ConfirmationNumber,
                                title = stringResource(R.string.profile_my_orders),
                                subtitle = stringResource(R.string.profile_orders_subtitle),
                                badge = if (state.activeCount > 0) pluralStringResource(R.plurals.active_orders, state.activeCount, state.activeCount) else null,
                                onClick = onMyOrders,
                            )
                            MenuDivider()
                            ProfileMenuItem(
                                icon = Icons.Filled.Translate,
                                title = stringResource(R.string.profile_language),
                                subtitle = stringResource(R.string.profile_language_subtitle),
                                onClick = onLanguage,
                            )
                            MenuDivider()
                            ProfileMenuItem(
                                icon = Icons.Filled.Edit,
                                title = stringResource(R.string.profile_edit),
                                subtitle = stringResource(R.string.profile_update_name),
                                onClick = { showEdit = true },
                            )
                            MenuDivider()
                            ProfileMenuItem(
                                icon = Icons.Filled.NotificationsNone,
                                title = stringResource(R.string.notifications_title),
                                subtitle = stringResource(R.string.profile_notification_subtitle),
                                badge = if (state.unreadCount > 0) stringResource(R.string.profile_new_badge, state.unreadCount.asBadgeCount()) else null,
                                onClick = onNotifications,
                            )
                            MenuDivider()
                            ProfileMenuItem(
                                icon = Icons.Filled.Storefront,
                                title = stringResource(R.string.profile_contact_shop),
                                subtitle = state.shop.displayName,
                                onClick = { state.shop.callNumber?.let { context.openDialer(it) } },
                            )
                            MenuDivider()
                            ProfileMenuItem(
                                icon = Icons.Filled.HelpOutline,
                                title = stringResource(R.string.profile_help_support),
                                subtitle = stringResource(R.string.profile_help_subtitle),
                                onClick = onHelp,
                            )
                            MenuDivider()
                            ProfileMenuItem(icon = Icons.Filled.Description, title = stringResource(R.string.profile_terms), onClick = onTerms)
                            MenuDivider()
                            ProfileMenuItem(icon = Icons.Filled.PrivacyTip, title = stringResource(R.string.profile_privacy), onClick = onPrivacy)
                            MenuDivider()
                            ProfileMenuItem(
                                icon = Icons.AutoMirrored.Filled.Logout,
                                title = stringResource(R.string.common_logout),
                                tint = PaanColors.Maroon,
                                showChevron = false,
                                onClick = { showLogout = true },
                            )
                        }
                    }
                }

                item {
                    Column(
                        Modifier.fillMaxWidth().padding(horizontal = Dimens.ScreenMargin),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        GoldDivider()
                        Text(
                            if (version.isNotEmpty()) stringResource(R.string.profile_footer_version, AppConfig.BRAND_NAME, version)
                            else stringResource(R.string.profile_footer, AppConfig.BRAND_NAME),
                            style = MaterialTheme.typography.labelSmall,
                            color = PaanColors.MutedBrown,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }

    if (showEdit) {
        EditNameDialog(
            initial = state.user?.name.orEmpty().let { if (it == "Guest") "" else it },
            phone = state.user?.phone.orEmpty(),
            validate = viewModel::validateName,
            onDismiss = { showEdit = false },
            onSave = { name, done ->
                viewModel.saveName(name) { error ->
                    if (error == null) showEdit = false
                    done(error)
                }
            },
        )
    }

    if (showLogout) {
        AlertDialog(
            onDismissRequest = { showLogout = false },
            title = { Text(stringResource(R.string.profile_logout_title)) },
            text = {
                Text(
                    stringResource(R.string.profile_logout_body),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showLogout = false
                    viewModel.logout(onLoggedOut)
                }) { Text(stringResource(R.string.common_logout), color = PaanColors.Maroon) }
            },
            dismissButton = {
                TextButton(onClick = { showLogout = false }) { Text(stringResource(R.string.common_cancel), color = PaanColors.ForestGreen) }
            },
            containerColor = PaanColors.Cream,
        )
    }
}
@Composable
private fun EditNameDialog(
    initial: String,
    phone: String,
    validate: (String) -> UiText?,
    onDismiss: () -> Unit,
    onSave: (String, (UiText?) -> Unit) -> Unit,
) {
    var name by remember { mutableStateOf(initial) }
    var tried by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var serverError by remember { mutableStateOf<UiText?>(null) }
    val error = serverError ?: if (tried) validate(name) else null

    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        title = { Text(stringResource(R.string.profile_edit)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { if (it.length <= ProfileViewModel.MAX_NAME_LENGTH) name = it },
                    label = { Text(stringResource(R.string.profile_name)) },
                    singleLine = true,
                    isError = error != null,
                    supportingText = error?.let { { Text(it.asString()) } },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PaanColors.ForestGreen,
                        focusedLabelColor = PaanColors.ForestGreen,
                        cursorColor = PaanColors.ForestGreen,
                    ),
                )
                if (phone.isNotBlank()) {
                    Text(
                        stringResource(R.string.profile_mobile_locked, phone.asIndianPhone()),
                        style = MaterialTheme.typography.labelSmall,
                        color = PaanColors.MutedBrown,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !saving,
                onClick = {
                    tried = true
                    serverError = null
                    if (validate(name) == null) {
                        saving = true
                        onSave(name) { err ->
                            saving = false
                            serverError = err
                        }
                    }
                },
            ) { Text(if (saving) stringResource(R.string.common_saving) else stringResource(R.string.common_save), color = PaanColors.ForestGreen) }
        },
        dismissButton = {
            TextButton(enabled = !saving, onClick = onDismiss) { Text(stringResource(R.string.common_cancel), color = PaanColors.MutedBrown) }
        },
        containerColor = PaanColors.Cream,
    )
}
@Composable
private fun MenuDivider() {
    HorizontalDivider(
        Modifier.padding(start = 56.dp),
        color = PaanColors.RoyalGold.copy(alpha = 0.25f),
    )
}

@Composable
private fun ProfileHeader(name: String, phone: String, collected: Int, onEdit: () -> Unit) {
    val displayName = name.ifBlank { stringResource(R.string.common_guest) }
    Column(
        Modifier
            .fillMaxWidth()
            .background(PaanColors.DeepForest)
            .statusBarsPadding()
            .padding(horizontal = Dimens.ScreenMargin, vertical = Dimens.SpaceLg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                Modifier
                    .size(88.dp)
                    .background(PaanColors.ForestGreen, CircleShape)
                    .border(3.dp, PaanColors.RoyalGold, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(displayName.asInitials(), style = MaterialTheme.typography.headlineLarge, color = PaanColors.Cream)
            }
            IconButton(
                onClick = onEdit,
                modifier = Modifier.size(32.dp).background(PaanColors.RoyalGold, CircleShape),
            ) {
                Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.a11y_edit_profile), tint = PaanColors.DarkBrown, modifier = Modifier.size(16.dp))
            }
        }
        Text(displayName, style = MaterialTheme.typography.headlineMedium, color = PaanColors.Cream)
        if (phone.isNotBlank()) {
            Text(phone.asIndianPhone(), style = MaterialTheme.typography.bodyMedium, color = PaanColors.LightGold)
        }
        Surface(shape = MaterialTheme.shapes.small, color = PaanColors.RoyalGold.copy(alpha = 0.2f)) {
            Text(
                pluralStringResource(R.plurals.orders_collected, collected, collected),
                Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelMedium,
                color = PaanColors.LightGold,
            )
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = PaanColors.CardWhite),
        border = BorderStroke(1.dp, PaanColors.RoyalGold.copy(alpha = 0.25f)),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(value, style = PaanTextStyles.Currency, color = PaanColors.ForestGreen)
            Text(label, style = MaterialTheme.typography.labelSmall, color = PaanColors.MutedBrown)
        }
    }
}

@Composable
private fun EditNameDialog(
    initial: String,
    phone: String,
    validate: (String) -> String?,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var name by remember { mutableStateOf(initial) }
    var tried by remember { mutableStateOf(false) }
    val error = if (tried) validate(name) else null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.profile_edit)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { if (it.length <= ProfileViewModel.MAX_NAME_LENGTH) name = it },
                    label = { Text(stringResource(R.string.profile_name)) },
                    singleLine = true,
                    isError = error != null,
                    supportingText = error?.let { { Text(it) } },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PaanColors.ForestGreen,
                        focusedLabelColor = PaanColors.ForestGreen,
                        cursorColor = PaanColors.ForestGreen,
                    ),
                )
                if (phone.isNotBlank()) {
                    Text(
                        stringResource(R.string.profile_mobile_locked, phone.asIndianPhone()),
                        style = MaterialTheme.typography.labelSmall,
                        color = PaanColors.MutedBrown,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                tried = true
                if (validate(name) == null) onSave(name)
            }) { Text(stringResource(R.string.common_save), color = PaanColors.ForestGreen) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel), color = PaanColors.MutedBrown) }
        },
        containerColor = PaanColors.Cream,
    )
}
