package com.mlopes.gestor.ui.config

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mlopes.gestor.BuildConfig
import com.mlopes.gestor.R
import com.mlopes.gestor.data.repository.AuthRepository
import com.mlopes.gestor.domain.usecase.SincronizarUseCase
import dagger.hilt.android.EntryPointAccessors
import androidx.compose.ui.platform.LocalContext
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import kotlinx.coroutines.launch

@EntryPoint
@InstallIn(ActivityComponent::class)
interface ConfigEntryPoint {
    fun authRepository(): AuthRepository
    fun sincronizarUseCase(): SincronizarUseCase
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(onSair: () -> Unit) {
    val context = LocalContext.current
    val entryPoint = remember {
        EntryPointAccessors.fromActivity(context as android.app.Activity, ConfigEntryPoint::class.java)
    }
    val authRepository = entryPoint.authRepository()
    val sincronizar = entryPoint.sincronizarUseCase()
    val scope = rememberCoroutineScope()
    var sincronizando by remember { mutableStateOf(false) }
    var mensagem by remember { mutableStateOf<String?>(null) }
    var confirmaSair by remember { mutableStateOf(false) }

    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.config_titulo)) }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(stringResource(R.string.config_api_titulo), style = MaterialTheme.typography.titleMedium)
            Text(BuildConfig.API_BASE_URL, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    scope.launch {
                        sincronizando = true
                        mensagem = null
                        sincronizar().fold(
                            onSuccess = { mensagem = context.getString(R.string.config_sincronizado) },
                            onFailure = { mensagem = context.getString(R.string.config_erro_sincronizar) },
                        )
                        sincronizando = false
                    }
                },
                enabled = !sincronizando,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (sincronizando) {
                    CircularProgressIndicator(modifier = Modifier.height(16.dp), strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.config_sincronizar))
                }
            }
            mensagem?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text(stringResource(R.string.config_sobre), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.config_sobre_versao), style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.config_sobre_autor), style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { confirmaSair = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.config_sair))
            }
        }
    }

    if (confirmaSair) {
        AlertDialog(
            onDismissRequest = { confirmaSair = false },
            title = { Text(stringResource(R.string.config_sair_confirmar)) },
            text = { Text(stringResource(R.string.config_sair_mensagem)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmaSair = false
                    scope.launch {
                        authRepository.logout()
                        onSair()
                    }
                }) { Text(stringResource(R.string.acao_confirmar)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmaSair = false }) {
                    Text(stringResource(R.string.acao_cancelar))
                }
            },
        )
    }
}
