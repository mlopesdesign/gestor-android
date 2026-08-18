package com.mlopes.gestor.ui.projetos

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
fun ProjetosScreen(viewModel: ProjetosViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.refresh() }
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.projetos_titulo)) }) }) { padding ->
        when {
            state.carregando -> LoadingIndicator(modifier = Modifier.padding(padding))
            state.erro != null -> ErrorState(
                mensagem = state.erro ?: stringResource(R.string.projetos_erro_carregar),
                onTentarNovamente = { viewModel.refresh() },
            )
            state.projetos.isEmpty() -> EmptyState(
                titulo = stringResource(R.string.projetos_vazia),
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(state.projetos, key = { it.id }) { p ->
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(p.nome, style = MaterialTheme.typography.titleMedium)
                            if (!p.descricao.isNullOrBlank()) {
                                Text(
                                    p.descricao,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Text(
                                "Status: ${p.status}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
