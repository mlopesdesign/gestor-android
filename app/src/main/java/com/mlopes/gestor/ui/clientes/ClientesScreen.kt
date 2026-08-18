package com.mlopes.gestor.ui.clientes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mlopes.gestor.R
import com.mlopes.gestor.ui.components.EmptyState
import com.mlopes.gestor.ui.components.ErrorState
import com.mlopes.gestor.ui.components.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientesScreen(viewModel: ClientesViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.refresh() }
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.clientes_titulo)) }) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = state.busca,
                onValueChange = viewModel::onBuscaChange,
                label = { Text(stringResource(R.string.clientes_busca_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            )
            when {
                state.carregando -> LoadingIndicator()
                state.clientes.isEmpty() -> EmptyState(titulo = stringResource(R.string.clientes_vazia))
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(state.clientes, key = { it.id }) { c ->
                        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(c.nome, style = MaterialTheme.typography.titleMedium)
                                if (!c.email.isNullOrBlank()) {
                                    Text(
                                        c.email,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
