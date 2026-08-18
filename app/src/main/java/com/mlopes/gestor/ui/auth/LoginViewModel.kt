package com.mlopes.gestor.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
) : ViewModel() {
    private val _state = MutableStateFlow(LoginUiState())
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

    private fun mensagem(e: Throwable): String = when (e) {
        is SecurityException -> e.message ?: "Erro de autenticacao."
        is java.net.UnknownHostException, is java.io.IOException -> "Sem conexao com a internet."
        else -> "Nao foi possivel entrar. Tente novamente."
    }
}
