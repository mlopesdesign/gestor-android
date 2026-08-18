package com.mlopes.gestor.domain.usecase

import com.mlopes.gestor.data.repository.SyncRepository
import javax.inject.Inject

class SincronizarUseCase @Inject constructor(
    private val repository: SyncRepository,
) {
    suspend operator fun invoke(): Result<Unit> = repository.sincronizarTudo()
}
