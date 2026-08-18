package com.mlopes.gestor.ui.tarefas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mlopes.gestor.data.repository.TarefaRepository
import com.mlopes.gestor.domain.model.Tarefa
import com.mlopes.gestor.domain.usecase.ConcluirTarefaUseCase
import com.mlopes.gestor.domain.usecase.FiltroTarefa
import com.mlopes.gestor.domain.usecase.ListarTarefasUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TarefasUiState(
    val carregando: Boolean = true,
    val tarefas: List<Tarefa> = emptyList(),
    val filtro: FiltroTarefa = FiltroTarefa.HOJE,
    val erro: String? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TarefasViewModel @Inject constructor(
    private val listarUseCase: ListarTarefasUseCase,
    private val concluirUseCase: ConcluirTarefaUseCase,
    private val tarefaRepository: TarefaRepository,
) : ViewModel() {
    private val filtroFlow = MutableStateFlow(FiltroTarefa.HOJE)

    private val tarefasFlow = filtroFlow.flatMapLatest { filtro ->
        listarUseCase(filtro)
    }

    val state: StateFlow<TarefasUiState> = combine(tarefasFlow, filtroFlow) { tarefas, filtro ->
        TarefasUiState(carregando = false, tarefas = tarefas, filtro = filtro)
    }
        .onEach { tarefaRepository.refresh() }
        .catch { e -> emit(TarefasUiState(carregando = false, erro = e.message)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TarefasUiState(),
        )

    fun setFiltro(filtro: FiltroTarefa) {
        filtroFlow.value = filtro
    }

    fun refresh() {
        viewModelScope.launch { tarefaRepository.refresh() }
    }

    fun concluir(id: String) {
        viewModelScope.launch { concluirUseCase(id) }
    }
}
