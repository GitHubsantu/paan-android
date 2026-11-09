package com.paan.safa.ui.screens.language

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.paan.safa.R
import com.paan.safa.core.localization.AppLanguage
import com.paan.safa.core.localization.LanguageStore
import com.paan.safa.core.localization.applyAppLanguage
import com.paan.safa.ui.components.PaanBackground
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LanguageViewModel @Inject constructor(private val languageStore: LanguageStore) : ViewModel() {
    fun select(language: AppLanguage) = languageStore.save(language)
}

@Composable
fun LanguageScreen(onDone: () -> Unit, viewModel: LanguageViewModel = hiltViewModel()) {
    val context = LocalContext.current

    fun choose(language: AppLanguage) {
        viewModel.select(language)                               // saves + switches the whole UI live
        applyAppLanguage(context.applicationContext, language)   // notifications / non-Compose strings
        onDone()                                                 // close immediately, no restart
    }

    PaanBackground {
        Column(
            Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(stringResource(R.string.language_title), style = MaterialTheme.typography.headlineMedium)
            Text(stringResource(R.string.language_subtitle), style = MaterialTheme.typography.bodyMedium)
            OutlinedButton(
                onClick = { choose(AppLanguage.English) },
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            ) { Text(stringResource(R.string.language_english)) }
            OutlinedButton(
                onClick = { choose(AppLanguage.Bengali) },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
            ) { Text(stringResource(R.string.language_bengali)) }
            OutlinedButton(
                onClick = { choose(AppLanguage.Odia) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.language_odia)) }
        }
    }
}