package com.mlopes.gestor.ui.projetos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mlopes.gestor.data.repository.ProjetoRepository
import com.mlopes.gestor.domain.model.Projeto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProjetosUiState(
    val carregando: Boolean = true,
    val projetos: List<Projeto> = emptyList(),
    val erro: String? = null,
)

@HiltViewModel
class ProjetosViewModel @Inject constructor(
    private val repository: ProjetoRepository,
) : ViewModel() {
    val state: StateFlow<ProjetosUiState> = repository.observar()
        .let { flow ->
            kotlinx.coroutines.flow.combine(flow, kotlinx.coroutines.flow.flowOf(Unit)) { lista, _ ->
                ProjetosUiState(carregando = false, projetos = lista)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ProjetosUiState(),
        )

    init { refresh() }

    fun refresh() {
        viewModelScope.launch { repository.refresh() }
    }
}
