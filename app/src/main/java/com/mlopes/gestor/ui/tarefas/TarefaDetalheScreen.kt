package com.mlopes.gestor.ui.tarefas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mlopes.gestor.R
import com.mlopes.gestor.domain.model.StatusTarefa
import com.mlopes.gestor.ui.components.ErrorState
import com.mlopes.gestor.ui.components.LoadingIndicator
import com.mlopes.gestor.ui.components.PrioridadeChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarefaDetalheScreen(
    tarefaId: String,
    onVoltar: () -> Unit,
    onEditar: () -> Unit,
    viewModel: TarefaDetalheViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var confirmaExcluir by remember { mutableStateOf(false) }

    LaunchedEffect(state.excluido) { if (state.excluido) onVoltar() }
    LaunchedEffect(state.finalizada) { if (state.finalizada) onVoltar() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tarefa_detalhe_titulo)) },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = onEditar) {
                        Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.tarefa_detalhe_acao_editar))
                    }
                    IconButton(onClick = { confirmaExcluir = true }) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.tarefa_detalhe_acao_excluir),
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                },
            )
        },
    ) { padding ->
        when {
            state.carregando -> LoadingIndicator(modifier = Modifier.padding(padding))
            state.erro != null -> ErrorState(mensagem = state.erro.orEmpty(), onTentarNovamente = viewModel::carregar)
            state.tarefa != null -> Conteudo(
                state = state,
                onConcluir = viewModel::concluir,
                onReabrir = viewModel::reabrir,
                modifier = Modifier.padding(padding),
            )
        }
    }

    if (confirmaExcluir) {
        AlertDialog(
            onDismissRequest = { confirmaExcluir = false },
            title = { Text(stringResource(R.string.tarefa_detalhe_confirmar_excluir)) },
            text = { Text(stringResource(R.string.tarefa_detalhe_excluir_mensagem)) },
            confirmButton = {
                TextButton(onClick = { confirmaExcluir = false; viewModel.excluir() }) {
                    Text(stringResource(R.string.acao_confirmar))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmaExcluir = false }) {
                    Text(stringResource(R.string.acao_cancelar))
                }
            },
        )
    }
}

@Composable
private fun Conteudo(
    state: TarefaDetalheUiState,
    onConcluir: () -> Unit,
    onReabrir: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tarefa = state.tarefa ?: return
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(tarefa.titulo, style = MaterialTheme.typography.headlineMedium)
        Text(
            text = tarefa.descricao ?: stringResource(R.string.tarefa_detalhe_sem_descricao),
            style = MaterialTheme.typography.bodyLarge,
            color = if (tarefa.descricao == null) MaterialTheme.colorScheme.onSurfaceVariant
            else MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(8.dp))
        LinhaRotulo(stringResource(R.string.tarefa_detalhe_status), tarefa.status.label())
        LinhaRotulo(stringResource(R.string.tarefa_detalhe_prioridade), "")
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            PrioridadeChip(prioridade = tarefa.prioridade)
        }
        LinhaRotulo(
            stringResource(R.string.tarefa_detalhe_vencimento),
            tarefa.vencimentoEm ?: stringResource(R.string.tarefa_detalhe_sem_vencimento),
        )
        LinhaRotulo(
            stringResource(R.string.tarefa_detalhe_projeto),
            tarefa.projetoId ?: stringResource(R.string.tarefa_detalhe_sem_projeto),
        )
        LinhaRotulo(
            stringResource(R.string.tarefa_detalhe_cliente),
            tarefa.clienteId ?: stringResource(R.string.tarefa_detalhe_sem_cliente),
        )
        Spacer(Modifier.height(16.dp))
        if (tarefa.status == StatusTarefa.CONCLUIDA) {
            Button(onClick = onReabrir, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.tarefa_detalhe_acao_reabrir))
            }
        } else {
            Button(
                onClick = onConcluir,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.tarefa_detalhe_acao_concluir))
            }
        }
    }
}

@Composable
private fun LinhaRotulo(rotulo: String, valor: String) {
    Column {
        Text(rotulo, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(valor, style = MaterialTheme.typography.bodyLarge)
    }
}

private fun StatusTarefa.label(): String = when (this) {
    StatusTarefa.CAIXA_ENTRADA -> "Caixa de entrada"
    StatusTarefa.PLANEJADA -> "Planejada"
    StatusTarefa.EM_ANDAMENTO -> "Em andamento"
    StatusTarefa.AGUARDANDO_TERCEIRO -> "Aguardando terceiro"
    StatusTarefa.BLOQUEADA -> "Bloqueada"
    StatusTarefa.EM_REVISAO -> "Em revisao"
    StatusTarefa.ENTREGUE_AGUARDANDO_CONFIRMACAO -> "Aguardando confirmacao"
    StatusTarefa.CONCLUIDA -> "Concluida"
    StatusTarefa.ADIADA -> "Adiada"
    StatusTarefa.CANCELADA -> "Cancelada"
    StatusTarefa.ARQUIVADA -> "Arquivada"
}
