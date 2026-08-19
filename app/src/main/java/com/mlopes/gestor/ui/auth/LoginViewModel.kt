package com.mlopes.gestor.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mlopes.gestor.data.remote.TokenStorage
import com.mlopes.gestor.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val senha: String = "",
    val carregando: Boolean = false,
    val erro: String? = null,
    val logado: Boolean = false,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val tokenStorage: TokenStorage,
) : ViewModel() {
    private val _state = MutableStateFlow(
        // Pre-preenche o email com o ultimo usado (UX: Marcio odeia redigitar).
        LoginUiState(email = tokenStorage.buscarEmail().orEmpty())
    )
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onEmailChange(valor: String) = _state.update { it.copy(email = valor, erro = null) }
    fun onSenhaChange(valor: String) = _state.update { it.copy(senha = valor, erro = null) }

    fun entrar() {
        val atual = _state.value
        if (atual.email.isBlank() || atual.senha.isBlank()) {
            _state.update { it.copy(erro = "Preencha e-mail e senha.") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(carregando = true, erro = null) }
            val resultado = loginUseCase(atual.email, atual.senha)
            resultado.fold(
                onSuccess = { _state.update { it.copy(carregando = false, logado = true) } },
                onFailure = { e -> _state.update { it.copy(carregando = false, erro = mensagem(e)) } },
            )
        }
    }

    private fun mensagem(e: Throwable): String {
        // Mostra a mensagem real do servidor quando existir (facilita debug).
        val real = e.message?.takeIf { it.isNotBlank() } ?: return "Nao foi possivel entrar. Tente novamente."
        return when {
            e is java.net.UnknownHostException -> "Sem conexao com a internet."
            e is java.net.ConnectException -> "Nao foi possivel conectar ao servidor."
            e is java.net.SocketTimeoutException -> "Tempo esgotado. Tente novamente."
            e is java.io.IOException -> "Falha de rede. Verifique sua conexao."
            else -> real
        }
    }
}
