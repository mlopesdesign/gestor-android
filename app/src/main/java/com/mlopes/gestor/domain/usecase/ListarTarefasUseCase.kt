package com.mlopes.gestor.domain.usecase

import com.mlopes.gestor.data.repository.TarefaRepository
import com.mlopes.gestor.domain.model.StatusTarefa
import com.mlopes.gestor.domain.model.Tarefa
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Filtro de listagem de tarefas.
 */
enum class FiltroTarefa {
    HOJE,
    PENDENTES,
    CONCLUIDAS,
    TODAS;
}

/**
 * Lista tarefas, aplicando filtro e ordenacao por prioridade/vencimento.
 */
class ListarTarefasUseCase @Inject constructor(
    private val repository: TarefaRepository,
) {
    operator fun invoke(filtro: FiltroTarefa = FiltroTarefa.HOJE): Flow<List<Tarefa>> {
        val status = when (filtro) {
            FiltroTarefa.PENDENTES -> StatusTarefa.PENDENTE
            FiltroTarefa.CONCLUIDAS -> StatusTarefa.CONCLUIDA
            else -> null
        }
        return repository.observarTarefas(status).map { tarefas ->
            if (filtro == FiltroTarefa.HOJE) tarefas.filtrarHoje() else tarefas
                .sortedWith(compareByDescending<Tarefa> { it.prioridade.peso }
                    .thenBy { it.vencimentoEm ?: "ZZZ" })
        }
    }

    private fun List<Tarefa>.filtrarHoje(): List<Tarefa> {
        val hoje = java.time.LocalDate.now().toString()
        return filter { it.vencimentoEm?.startsWith(hoje) == true || it.status != StatusTarefa.CONCLUIDA }
            .sortedWith(compareByDescending<Tarefa> { it.prioridade.peso }
                .thenBy { it.vencimentoEm ?: "ZZZ" })
    }
}
