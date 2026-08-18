package com.mlopes.gestor.ui.tarefas

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mlopes.gestor.data.repository.TarefaRepository
import com.mlopes.gestor.domain.model.Tarefa
import com.mlopes.gestor.domain.usecase.ConcluirTarefaUseCase
import com.mlopes.gestor.domain.usecase.ExcluirTarefaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TarefaDetalheUiState(
    val carregando: Boolean = true,
    val tarefa: Tarefa? = null,
    val erro: String? = null,
    val excluido: Boolean = false,
    val finalizada: Boolean = false,
)

@HiltViewModel
class TarefaDetalheViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: TarefaRepository,
    private val concluirUseCase: ConcluirTarefaUseCase,
    private val excluirUseCase: ExcluirTarefaUseCase,
) : ViewModel() {
    val tarefaId: String = savedStateHandle.get<String>("id").orEmpty()
    private val _state = MutableStateFlow(TarefaDetalheUiState())
    val state: StateFlow<TarefaDetalheUiState> = _state.asStateFlow()

    init {
        carregar()
    }

    fun carregar() {
        viewModelScope.launch {
            _state.update { it.copy(carregando = true, erro = null) }
            repository.buscarPorId(tarefaId).fold(
                onSuccess = { t -> _state.update { it.copy(carregando = false, tarefa = t) } },
                onFailure = { e -> _state.update { it.copy(carregando = false, erro = e.message) } },
            )
        }
    }

    fun concluir() {
        viewModelScope.launch {
            concluirUseCase(tarefaId).onSuccess { _state.update { it.copy(finalizada = true) } }
        }
    }

    fun reabrir() {
        viewModelScope.launch {
            repository.reabrir(tarefaId)
            carregar()
        }
    }

    fun excluir() {
        viewModelScope.launch {
            excluirUseCase(tarefaId).onSuccess { _state.update { it.copy(excluido = true) } }
        }
    }
}
