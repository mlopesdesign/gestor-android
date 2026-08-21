package com.mlopes.gestor.ui.tarefas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.navigation.NavHostController
import com.mlopes.gestor.R
import com.mlopes.gestor.domain.usecase.FiltroTarefa
import com.mlopes.gestor.ui.components.EmptyState
import com.mlopes.gestor.ui.components.ErrorState
import com.mlopes.gestor.ui.components.LoadingIndicator
import com.mlopes.gestor.ui.components.TarefaCard
import com.mlopes.gestor.ui.nav.Route

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarefasScreen(
    navController: NavHostController,
    inicial: FiltroTarefa,
    viewModel: TarefasViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(inicial) { viewModel.setFiltro(inicial) }
    LaunchedEffect(Unit) { viewModel.refresh() }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.tarefas_titulo)) })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Route.TarefaEditar.criar(null)) }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.tarefas_acao_nova))
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            FiltroTarefas(
                atual = state.filtro,
                onSelecionar = viewModel::setFiltro,
            )
            when {
                state.carregando -> LoadingIndicator()
                state.erro != null -> ErrorState(
                    mensagem = state.erro ?: stringResource(R.string.tarefas_erro_carregar),
                    onTentarNovamente = { viewModel.refresh() },
                )
                state.tarefas.isEmpty() -> EmptyState()
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    items(state.tarefas, key = { it.id }) { tarefa ->
                        TarefaCard(
                            tarefa = tarefa,
                            onClick = { navController.navigate(Route.TarefaDetalhe.criar(tarefa.id)) },
                            onConcluir = if (tarefa.status != com.mlopes.gestor.domain.model.StatusTarefa.CONCLUIDA) {
                                { viewModel.concluir(tarefa.id) }
                            } else null,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FiltroTarefas(
    atual: FiltroTarefa,
    onSelecionar: (FiltroTarefa) -> Unit,
) {
    val opcoes = listOf(
        FiltroTarefa.HOJE to R.string.tarefas_filtro_hoje,
        FiltroTarefa.PENDENTES to R.string.tarefas_filtro_pendentes,
        FiltroTarefa.CONCLUIDAS to R.string.tarefas_filtro_concluidas,
        FiltroTarefa.TODAS to R.string.tarefas_filtro_todas,
    )
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        opcoes.forEachIndexed { index, (filtro, label) ->
            SegmentedButton(
                selected = atual == filtro,
                onClick = { onSelecionar(filtro) },
                shape = SegmentedButtonDefaults.itemShape(index, opcoes.size),
            ) {
                Text(stringResource(label), style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}
