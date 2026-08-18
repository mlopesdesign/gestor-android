package com.mlopes.gestor.domain.usecase

import com.mlopes.gestor.data.repository.TarefaRepository
import javax.inject.Inject

class ExcluirTarefaUseCase @Inject constructor(
    private val repository: TarefaRepository,
) {
    suspend operator fun invoke(id: String): Result<Unit> = repository.excluir(id)
}
