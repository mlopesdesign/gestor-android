package com.mlopes.gestor.domain.usecase

import com.mlopes.gestor.data.repository.TarefaRepository
import com.mlopes.gestor.domain.model.Tarefa
import javax.inject.Inject

class AtualizarTarefaUseCase @Inject constructor(
    private val repository: TarefaRepository,
) {
    suspend operator fun invoke(id: String, input: TarefaInput): Result<Tarefa> {
        if (input.titulo.isBlank()) {
            return Result.failure(IllegalArgumentException("Titulo obrigatorio."))
        }
        return repository.atualizar(id, input)
    }
}
