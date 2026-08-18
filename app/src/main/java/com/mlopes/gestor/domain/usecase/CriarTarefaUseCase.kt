package com.mlopes.gestor.domain.usecase

import com.mlopes.gestor.data.repository.TarefaRepository
import com.mlopes.gestor.domain.model.Prioridade
import com.mlopes.gestor.domain.model.StatusTarefa
import com.mlopes.gestor.domain.model.Tarefa
import javax.inject.Inject

data class TarefaInput(
    val titulo: String,
    val descricao: String? = null,
    val projetoId: String? = null,
    val clienteId: String? = null,
    val prioridade: Prioridade = Prioridade.NORMAL,
    val status: StatusTarefa = StatusTarefa.PENDENTE,
    val vencimentoEm: String? = null,
)

class CriarTarefaUseCase @Inject constructor(
    private val repository: TarefaRepository,
) {
    suspend operator fun invoke(input: TarefaInput): Result<Tarefa> {
        if (input.titulo.isBlank()) {
            return Result.failure(IllegalArgumentException("Titulo obrigatorio."))
        }
        return repository.criar(input)
    }
}
