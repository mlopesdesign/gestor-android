package com.mlopes.gestor.data.remote

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.mlopes.gestor.data.remote.api.AuthApi
import com.mlopes.gestor.data.remote.api.FcmTokenRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Gerencia o token FCM do dispositivo.
 *
 * - Persiste o token em EncryptedSharedPreferences (gestor_fcm)
 * - Envia pro WP via /sync/fcm-token quando muda
 * - Expõe StateFlow pro ViewModel saber se tem token ativo
 *
 * FIX v0.1.5: criado pra teste de FCM (F6 Notificacoes).
 */
@Singleton
class FcmTokenStorage @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authApi: AuthApi,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val prefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "gestor_fcm",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> get() = _token

    init {
        _token.value = prefs.getString(KEY_TOKEN, null)
    }

    /**
     * Salva o token localmente e envia pro WP em background.
     * Se o WP retornar erro, nao bloqueia — a proxima chamada tenta de novo.
     */
    fun atualizarToken(novoToken: String) {
        if (_token.value == novoToken) return
        _token.value = novoToken
        prefs.edit().putString(KEY_TOKEN, novoToken).apply()
        scope.launch {
            runCatching {
                authApi.enviarFcmToken(FcmTokenRequest(token = novoToken, plataforma = "ANDROID"))
            }
        }
    }

    fun limpar() {
        _token.value = null
        prefs.edit().remove(KEY_TOKEN).apply()
    }

    private companion object {
        const val KEY_TOKEN = "fcm_token"
    }
}
