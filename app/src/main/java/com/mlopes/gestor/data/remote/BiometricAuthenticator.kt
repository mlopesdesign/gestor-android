package com.mlopes.gestor.data.remote

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FIX v0.1.4: wrapper de biometria do AndroidX. Permite login com digital/face
 * sem digitar a senha (desde que tenha credencial salva via "Lembrar de mim").
 *
 * Tipos aceitos: BIOMETRIC_WEAK (reconhecimento facial basico, sem hardware seguro)
 * e BIOMETRIC_STRONG (impressao digital, face ID com hardware seguro).
 * Rejeita: BIOMETRIC_ERROR_NONE_ENROLLED (sem biometria cadastrada no aparelho).
 */
@Singleton
class BiometricAuthenticator @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    /**
     * Retorna true se o aparelho TEM biometria disponivel E o usuario ja cadastrou
     * alguma (digital/face). Usado pra mostrar/ocultar o botao "Entrar com biometria".
     */
    fun disponivel(): Boolean {
        val mgr = BiometricManager.from(context)
        val autenticadores = BiometricManager.Authenticators.BIOMETRIC_WEAK or
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        return mgr.canAuthenticate(autenticadores) == BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Mostra o prompt biometric do AndroidX e invoca onSuccess(true) se o usuario
     * autenticou com sucesso, onFailure(senhaErrada) caso contrario.
     *
     * @param activity FragmentActivity do app (MainActivity).
     * @param titulo titulo do prompt (ex: "Entrar no Gestor").
     * @param subtitulo subtitulo (ex: "Confirme sua digital").
     * @param onSuccess chamado quando autenticacao foi aceita.
     * @param onFailure chamado em qualquer outro caso (cancelou, falhou, etc).
     */
    fun autenticar(
        activity: FragmentActivity,
        titulo: String = "Entrar no Gestor",
        subtitulo: String = "Confirme sua digital para entrar",
        onSuccess: () -> Unit,
        onFailure: (mensagem: String) -> Unit,
    ) {
        val executor = ContextCompat.getMainExecutor(context)
        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onFailure(errString.toString())
                }
                override fun onAuthenticationFailed() {
                    // usuario存在 mas digital/face nao reconhecida. NAO chamar onFailure
                    // pq o sistema ainda permite tentar de novo. O proprio BiometricPrompt
                    // mostra o aviso e permite nova tentativa.
                }
            },
        )
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(titulo)
            .setSubtitle(subtitulo)
            .setNegativeButtonText("Usar senha")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_WEAK or
                    BiometricManager.Authenticators.BIOMETRIC_STRONG
            )
            .build()
        prompt.authenticate(info)
    }
}
