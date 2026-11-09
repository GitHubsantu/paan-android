package com.paan.safa.ui.screens.registration

import com.paan.safa.data.remote.asUiText
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paan.safa.R
import com.paan.safa.core.ui.UiText
import com.paan.safa.core.ui.asString
import com.paan.safa.domain.repository.AuthRepository
import com.paan.safa.ui.components.PaanBackground
import com.paan.safa.ui.components.PrimaryPaanButton
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompleteProfileViewModel @Inject constructor(private val auth: AuthRepository) : ViewModel() {
    fun save(name: String, onResult: (UiText?) -> Unit) {
        val cleanName = name.trim()
        val error = when {
            cleanName.length < 2 -> UiText.Res(R.string.error_name_short)
            cleanName.length > 40 -> UiText.Res(R.string.error_name_long)
            else -> null
        }
        if (error != null) { onResult(error); return }
        viewModelScope.launch {
            onResult(auth.updateName(cleanName).exceptionOrNull()?.asUiText())
        }
    }
}

@Composable
fun CompleteProfileScreen(onDone: () -> Unit, viewModel: CompleteProfileViewModel = hiltViewModel()) {
    var name by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<UiText?>(null) }
    var saving by remember { mutableStateOf(false) }
    PaanBackground {
        Column(
            Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(stringResource(R.string.registration_title), style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.registration_subtitle), style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { if (it.length <= 40) { name = it; error = null } },
                label = { Text(stringResource(R.string.registration_name_label)) },
                isError = error != null,
                supportingText = error?.let { { Text(it.asString()) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))
            PrimaryPaanButton(
                text = stringResource(R.string.registration_continue),
                enabled = !saving,
                isLoading = saving,
                onClick = {
                    saving = true
                    viewModel.save(name) { result ->
                        saving = false
                        error = result
                        if (result == null) onDone()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
