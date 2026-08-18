package com.mlopes.gestor.domain.usecase

import com.mlopes.gestor.data.repository.AuthRepository
import javax.inject.Inject

/**
 * Realiza login e armazena o token de forma segura.
 */
class LoginUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(email: String, senha: String): Result<Unit> {
        val emailLimpo = email.trim()
        if (emailLimpo.isEmpty() || senha.isEmpty()) {
            return Result.failure(IllegalArgumentException("Preencha e-mail e senha."))
        }
        return repository.login(emailLimpo, senha)
    }
}
