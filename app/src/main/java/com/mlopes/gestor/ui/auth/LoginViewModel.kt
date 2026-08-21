package com.mlopes.gestor.ui.auth

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mlopes.gestor.data.remote.BiometricAuthenticator
import com.mlopes.gestor.data.remote.CredentialsStorage
import com.mlopes.gestor.data.remote.TokenStorage
import com.mlopes.gestor.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * FIX v0.1.4: LoginViewModel com 4 melhorias de UX:
 *  - Pre-preenche email/senha salvos ("Lembrar de mim" no login anterior).
 *  - Expor `mostrarSenha` (toggle do olho) e `lembrar` (checkbox).
 *  - Botao de biometria aparece se [BiometricAuthenticator.disponivel] for true
 *    E o usuario tiver credencial salva.
 *  - Botao "Entrar com biometria" chama login automatico apos autenticacao.
 */
data class LoginUiState(
    val email: String = "",
    val senha: String = "",
    val mostrarSenha: Boolean = false,
    val lembrar: Boolean = false,
    val biometriaDisponivel: Boolean = false,
    val carregando: Boolean = false,
    val erro: String? = null,
    val logado: Boolean = false,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val tokenStorage: TokenStorage,
    private val credentialsStorage: CredentialsStorage,
    private val biometricAuthenticator: BiometricAuthenticator,
) : ViewModel() {
    private val _state = MutableStateFlow(
        LoginUiState(
            email = credentialsStorage.buscarEmail() ?: tokenStorage.buscarEmail().orEmpty(),
            senha = if (credentialsStorage.lembrar()) credentialsStorage.buscarSenha().orEmpty() else "",
            lembrar = credentialsStorage.lembrar(),
            biometriaDisponivel = credentialsStorage.temCredencialSalva() &&
                biometricAuthenticator.disponivel(),
        ),
    )
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onEmailChange(valor: String) = _state.update { it.copy(email = valor, erro = null) }
    fun onSenhaChange(valor: String) = _state.update { it.copy(senha = valor, erro = null) }
    fun onMostrarSenhaToggle() = _state.update { it.copy(mostrarSenha = !it.mostrarSenha) }
    fun onLembrarToggle(valor: Boolean) = _state.update { it.copy(lembrar = valor) }

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
                onSuccess = {
                    if (atual.lembrar) {
                        credentialsStorage.salvar(atual.email, atual.senha)
                    } else {
                        credentialsStorage.limpar()
                    }
                    _state.update { it.copy(carregando = false, logado = true) }
                },
                onFailure = { e -> _state.update { it.copy(carregando = false, erro = mensagem(e)) } },
            )
        }
    }

    /**
     * Chamado pelo botao "Entrar com digital". Mostra o prompt biometric e,
     * se o usuario autenticar, faz login automatico com a credencial salva.
     */
    fun entrarComBiometria(activity: FragmentActivity) {
        if (!credentialsStorage.temCredencialSalva()) {
            _state.update { it.copy(erro = "Nenhuma credencial salva. Faca login normal primeiro.") }
            return
        }
        val email = credentialsStorage.buscarEmail().orEmpty()
        val senha = credentialsStorage.buscarSenha().orEmpty()
        biometricAuthenticator.autenticar(
            activity = activity,
            titulo = "Entrar no Gestor",
            subtitulo = "Confirme sua digital para entrar como $email",
            onSuccess = {
                viewModelScope.launch {
                    _state.update { it.copy(carregando = true, erro = null, email = email, senha = senha) }
                    val resultado = loginUseCase(email, senha)
                    resultado.fold(
                        onSuccess = { _state.update { it.copy(carregando = false, logado = true) } },
                        onFailure = { e -> _state.update { it.copy(carregando = false, erro = mensagem(e)) } },
                    )
                }
            },
            onFailure = { msg -> _state.update { it.copy(erro = "Biometria: $msg") } },
        )
    }

    private fun mensagem(e: Throwable): String {
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
