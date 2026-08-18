package com.mlopes.gestor.ui.areas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mlopes.gestor.data.repository.AreaRepository
import com.mlopes.gestor.domain.model.Area
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AreasUiState(
    val carregando: Boolean = true,
    val areas: List<Area> = emptyList(),
)

@HiltViewModel
class AreasViewModel @Inject constructor(
    private val repository: AreaRepository,
) : ViewModel() {
    val state: StateFlow<AreasUiState> = repository.observar()
        .let { flow ->
            kotlinx.coroutines.flow.combine(flow, kotlinx.coroutines.flow.flowOf(Unit)) { lista, _ ->
                AreasUiState(carregando = false, areas = lista)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AreasUiState(),
        )

    init { refresh() }
    fun refresh() { viewModelScope.launch { repository.refresh() } }
}
