package com.mlopes.gestor.ui.tarefas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.mlopes.gestor.domain.model.Prioridade
import com.mlopes.gestor.domain.model.StatusTarefa
import com.mlopes.gestor.ui.components.PrioridadeChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarefaEditarScreen(
    tarefaId: String?,
    onVoltar: () -> Unit,
    viewModel: TarefaEditarViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val projetos by viewModel.projetos.collectAsStateWithLifecycle()
    val clientes by viewModel.clientes.collectAsStateWithLifecycle()

    LaunchedEffect(state.salvo) { if (state.salvo) onVoltar() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (tarefaId == null) R.string.tarefa_editar_titulo_nova
                            else R.string.tarefa_editar_titulo_editar
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = state.titulo,
                onValueChange = viewModel::onTituloChange,
                label = { Text(stringResource(R.string.tarefa_editar_campo_titulo)) },
                singleLine = true,
                isError = state.erro != null && state.titulo.isBlank(),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.descricao,
                onValueChange = viewModel::onDescricaoChange,
                label = { Text(stringResource(R.string.tarefa_editar_campo_descricao)) },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(stringResource(R.string.tarefa_editar_campo_prioridade), style = MaterialTheme.typography.labelLarge)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Prioridade.values().forEach { p ->
                    SelecionavelPrioridade(
                        prioridade = p,
                        selecionado = state.prioridade == p,
                        onSelecionar = { viewModel.onPrioridadeChange(p) },
                    )
                }
            }

            Dropdown(
                rotulo = stringResource(R.string.tarefa_editar_campo_projeto),
                opcoes = projetos.map { it.id to it.nome },
                selecionado = state.projetoId,
                onSelecionar = viewModel::onProjetoChange,
                vazio = stringResource(R.string.tarefa_editar_sem_projeto),
            )
            Dropdown(
                rotulo = stringResource(R.string.tarefa_editar_campo_cliente),
                opcoes = clientes.map { it.id to it.nome },
                selecionado = state.clienteId,
                onSelecionar = viewModel::onClienteChange,
                vazio = stringResource(R.string.tarefa_editar_sem_cliente),
            )

            if (state.erro != null) {
                Text(
                    state.erro.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Button(
                onClick = viewModel::salvar,
                enabled = !state.salvando,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.tarefa_editar_salvar))
            }
        }
    }
}

@Composable
private fun SelecionavelPrioridade(
    prioridade: Prioridade,
    selecionado: Boolean,
    onSelecionar: () -> Unit,
) {
    if (selecionado) {
        Button(onClick = onSelecionar) { PrioridadeChip(prioridade) }
    } else {
        OutlinedButton(onClick = onSelecionar) { PrioridadeChip(prioridade) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Dropdown(
    rotulo: String,
    opcoes: List<Pair<String, String>>,
    selecionado: String?,
    onSelecionar: (String?) -> Unit,
    vazio: String,
) {
    var expanded by remember { mutableStateOf(false) }
    val labelAtual = opcoes.firstOrNull { it.first == selecionado }?.second ?: vazio
    Column {
        OutlinedTextField(
            value = labelAtual,
            onValueChange = {},
            readOnly = true,
            label = { Text(rotulo) },
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
            OutlinedButton(onClick = { expanded = true }) { Text("Selecionar") }
            if (selecionado != null) {
                OutlinedButton(onClick = { onSelecionar(null) }) { Text(vazio) }
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(vazio) },
                onClick = { onSelecionar(null); expanded = false },
            )
            opcoes.forEach { (id, nome) ->
                DropdownMenuItem(
                    text = { Text(nome) },
                    onClick = { onSelecionar(id); expanded = false },
                )
            }
        }
    }
}
