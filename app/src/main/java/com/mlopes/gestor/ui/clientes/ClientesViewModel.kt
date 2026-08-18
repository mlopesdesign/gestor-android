package com.mlopes.gestor.ui.clientes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mlopes.gestor.data.repository.ClienteRepository
import com.mlopes.gestor.domain.model.Cliente
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ClientesUiState(
    val carregando: Boolean = true,
    val clientes: List<Cliente> = emptyList(),
    val busca: String = "",
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ClientesViewModel @Inject constructor(
    private val repository: ClienteRepository,
) : ViewModel() {
    private val buscaFlow = MutableStateFlow("")

    val state: StateFlow<ClientesUiState> = buscaFlow
        .flatMapLatest { q -> repository.buscar(q) }
        .let { flow ->
            kotlinx.coroutines.flow.combine(flow, buscaFlow) { lista, q ->
                ClientesUiState(carregando = false, clientes = lista, busca = q)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ClientesUiState(),
        )

    init { refresh() }

    fun onBuscaChange(v: String) { buscaFlow.value = v }
    fun refresh() { viewModelScope.launch { repository.refresh() } }
}
