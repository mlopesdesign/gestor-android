package com.mlopes.gestor.ui.tarefas

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mlopes.gestor.data.repository.AreaRepository
import com.mlopes.gestor.data.repository.ClienteRepository
import com.mlopes.gestor.data.repository.ProjetoRepository
import com.mlopes.gestor.data.repository.TarefaRepository
import com.mlopes.gestor.domain.model.Area
import com.mlopes.gestor.domain.model.Cliente
import com.mlopes.gestor.domain.model.Prioridade
import com.mlopes.gestor.domain.model.Projeto
import com.mlopes.gestor.domain.model.StatusTarefa
import com.mlopes.gestor.domain.model.Tarefa
import com.mlopes.gestor.domain.usecase.AtualizarTarefaUseCase
import com.mlopes.gestor.domain.usecase.CriarTarefaUseCase
import com.mlopes.gestor.domain.usecase.TarefaInput
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TarefaEditarUiState(
    val carregando: Boolean = false,
    val salvando: Boolean = false,
    val titulo: String = "",
    val descricao: String = "",
    val projetoId: String? = null,
    val clienteId: String? = null,
    val prioridade: Prioridade = Prioridade.NORMAL,
    val status: StatusTarefa = StatusTarefa.PLANEJADA,
    val vencimentoEm: String? = null,
    val erro: String? = null,
    val salvo: Boolean = false,
)

@HiltViewModel
class TarefaEditarViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val tarefaRepository: TarefaRepository,
    private val projetoRepository: ProjetoRepository,
    private val clienteRepository: ClienteRepository,
    private val areaRepository: AreaRepository,
    private val criarUseCase: CriarTarefaUseCase,
    private val atualizarUseCase: AtualizarTarefaUseCase,
) : ViewModel() {
    val tarefaId: String? = savedStateHandle.get<String>("id")?.takeIf { it.isNotBlank() }

    private val _state = MutableStateFlow(TarefaEditarUiState())
    val state: StateFlow<TarefaEditarUiState> = _state.asStateFlow()

    val projetos: StateFlow<List<Projeto>> = projetoRepository.observar()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val clientes: StateFlow<List<Cliente>> = clienteRepository.observar()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val areas: StateFlow<List<Area>> = areaRepository.observar()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        if (tarefaId != null) carregar(tarefaId)
    }

    private fun carregar(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(carregando = true) }
            tarefaRepository.buscarPorId(id).fold(
                onSuccess = { t ->
                    _state.update {
                        it.copy(
                            carregando = false,
                            titulo = t.titulo,
                            descricao = t.descricao.orEmpty(),
                            projetoId = t.projetoId,
                            clienteId = t.clienteId,
                            prioridade = t.prioridade,
                            status = t.status,
                            vencimentoEm = t.vencimentoEm,
                        )
                    }
                },
                onFailure = { e -> _state.update { it.copy(carregando = false, erro = e.message) } },
            )
        }
    }

    fun onTituloChange(v: String) = _state.update { it.copy(titulo = v, erro = null) }
    fun onDescricaoChange(v: String) = _state.update { it.copy(descricao = v) }
    fun onPrioridadeChange(v: Prioridade) = _state.update { it.copy(prioridade = v) }
    fun onStatusChange(v: StatusTarefa) = _state.update { it.copy(status = v) }
    fun onProjetoChange(v: String?) = _state.update { it.copy(projetoId = v) }
    fun onClienteChange(v: String?) = _state.update { it.copy(clienteId = v) }
    fun onVencimentoChange(v: String?) = _state.update { it.copy(vencimentoEm = v) }

    fun salvar() {
        val s = _state.value
        if (s.titulo.isBlank()) {
            _state.update { it.copy(erro = "Informe um titulo.") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(salvando = true) }
            val input = TarefaInput(
                titulo = s.titulo,
                descricao = s.descricao.takeIf { it.isNotBlank() },
                projetoId = s.projetoId,
                clienteId = s.clienteId,
                prioridade = s.prioridade,
                status = s.status,
                vencimentoEm = s.vencimentoEm,
            )
            val resultado = if (tarefaId == null) criarUseCase(input) else atualizarUseCase(tarefaId, input)
            resultado.fold(
                onSuccess = { _state.update { it.copy(salvando = false, salvo = true) } },
                onFailure = { e -> _state.update { it.copy(salvando = false, erro = e.message) } },
            )
        }
    }
}
