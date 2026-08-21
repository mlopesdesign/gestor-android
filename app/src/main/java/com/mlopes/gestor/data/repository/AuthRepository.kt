package com.mlopes.gestor.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.mlopes.gestor.data.remote.TokenStorage
import com.mlopes.gestor.data.remote.api.AuthApi
import com.mlopes.gestor.data.remote.dto.ApiEnvelope
import com.mlopes.gestor.data.remote.dto.LoginData
import com.mlopes.gestor.data.remote.dto.LoginRequest
import com.mlopes.gestor.data.remote.dto.UsuarioDto
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Operacoes de autenticacao. Encapsula chamadas a AuthApi + persistencia do token.
 */
@Singleton
class AuthRepository @Inject constructor(
    private val api: AuthApi,
    private val tokenStorage: TokenStorage,
    @ApplicationContext context: Context,
) {
    // FIX v0.1.3: ID de dispositivo estavel por instalacao.
    // ANTES: retornava UUID.randomUUID() a cada abertura, o que fazia o WP
    // criar um sync_cursores novo a cada vez (sync nunca progredia).
    // AGORA: gera 1x e persiste em SharedPreferences normal (nao precisa
    // criptografar - e' um identificador publico do dispositivo).
    private val devicePrefs: SharedPreferences =
        context.getSharedPreferences("gestor_device", Context.MODE_PRIVATE)

    fun dispositivoId(): String {
        val existing = devicePrefs.getString(KEY_DISPOSITIVO_ID, null)
        if (existing != null) return existing
        val novo = "android-" + UUID.randomUUID().toString().lowercase()
        devicePrefs.edit().putString(KEY_DISPOSITIVO_ID, novo).apply()
        return novo
    }

    fun logado(): Boolean = tokenStorage.buscar()?.isNotEmpty() == true && !tokenStorage.expirou()

    suspend fun login(email: String, senha: String): Result<Unit> = runCatching {
        val response = api.login(LoginRequest(email = email, senha = senha, dispositivoId = dispositivoId()))
        if (!response.isSuccessful) {
            throw mapearErro(response.code(), response.errorBody()?.string())
        }
        val payload = response.body()?.data
            ?: throw IllegalStateException("Resposta vazia do servidor.")
        if (payload.token.isBlank()) {
            throw IllegalStateException("Token nao retornado.")
        }
        tokenStorage.salvar(payload.token, Instant.parse(payload.expiraEm))
        // Lembra o email pra pre-preencher na proxima abertura do app.
        tokenStorage.salvarEmail(email)
    }

    suspend fun logout(): Result<Unit> = runCatching {
        runCatching { api.logout() } // best-effort
        tokenStorage.limpar()
    }

    suspend fun eu(): Result<UsuarioDto> = runCatching {
        val response = api.me()
        if (!response.isSuccessful) throw mapearErro(response.code(), null)
        response.body()?.data ?: throw IllegalStateException("Resposta vazia.")
    }

    private fun mapearErro(status: Int, body: String?): Exception {
        return when (status) {
            401 -> SecurityException("E-mail ou senha incorretos.")
            429 -> SecurityException("Muitas tentativas. Aguarde 15 minutos.")
            else -> java.io.IOException("Falha de rede (HTTP $status).")
        }
    }

    companion object {
        private const val KEY_DISPOSITIVO_ID = "dispositivo_id"
    }
}
