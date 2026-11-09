package com.paan.safa.ui.screens.info

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.annotation.StringRes
import com.paan.safa.R
import androidx.compose.ui.unit.dp
import com.paan.safa.ui.components.PaanBackground
import com.paan.safa.ui.theme.Dimens
import com.paan.safa.ui.theme.PaanColors

data class InfoSection(@StringRes val headingRes: Int, @StringRes val bodyRes: Int)

/** Simple text pages. Replace the placeholder legal text with your real content. */
object InfoContent {
    val notifications = listOf(
        InfoSection(R.string.info_alerts_heading, R.string.info_alerts_body),
    )

    val help = listOf(
        InfoSection(R.string.info_pickup_heading, R.string.info_pickup_body),
        InfoSection(R.string.info_delivery_heading, R.string.info_delivery_body),
        InfoSection(R.string.info_cancel_heading, R.string.info_cancel_body),
        InfoSection(R.string.info_time_heading, R.string.info_time_body),
        InfoSection(R.string.info_payment_heading, R.string.info_payment_body),
    )

    // TODO: replace with your real Terms of Service text.
    val terms = listOf(
        InfoSection(R.string.info_terms_heading, R.string.info_terms_body),
    )

    // TODO: replace with your real Privacy Policy text.
    val privacy = listOf(
        InfoSection(R.string.info_privacy_heading, R.string.info_privacy_body),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoScreen(@StringRes title: Int, sections: List<InfoSection>, onBack: () -> Unit) {
    Scaffold(
        containerColor = PaanColors.Cream,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(title), style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.a11y_back)) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PaanColors.Cream),
            )
        },
    ) { padding ->
        PaanBackground(Modifier.padding(padding)) {
            LazyColumn(
                contentPadding = PaddingValues(Dimens.ScreenMargin),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
            ) {
                items(sections) { s ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(stringResource(s.headingRes), style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(s.bodyRes), style = MaterialTheme.typography.bodyMedium, color = PaanColors.MutedBrown)
                    }
                }
            }
        }
    }
}
