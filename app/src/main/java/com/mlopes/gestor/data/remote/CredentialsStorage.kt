package com.mlopes.gestor.data.remote

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FIX v0.1.4: armazena credenciais (email + senha) criptografadas pro login automatico.
 * Diferente do TokenStorage (que guarda so o token Bearer), aqui vai o email/senha
 * de fato - usado pra preencher o form e/ou autenticar via biometria.
 *
 * Storage: EncryptedSharedPreferences (AES-256-GCM) - mesmo padrao do TokenStorage.
 *
 * IMPORTANTE: NUNCA persiste senha sem o consentimento explicito do usuario
 * (checkbox "Lembrar de mim"). Limpar via [limpar] ao desmarcar.
 */
@Singleton
class CredentialsStorage @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "gestor_credentials",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    fun salvar(email: String, senha: String) {
        prefs.edit()
            .putString(KEY_EMAIL, email)
            .putString(KEY_SENHA, senha)
            .putBoolean(KEY_LEMBRAR, true)
            .apply()
    }

    fun buscarEmail(): String? = prefs.getString(KEY_EMAIL, null)
    fun buscarSenha(): String? = prefs.getString(KEY_SENHA, null)
    fun lembrar(): Boolean = prefs.getBoolean(KEY_LEMBRAR, false)

    fun temCredencialSalva(): Boolean =
        lembrar() && !buscarEmail().isNullOrBlank() && !buscarSenha().isNullOrBlank()

    fun limpar() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_EMAIL = "email_salvo"
        private const val KEY_SENHA = "senha_salva"
        private const val KEY_LEMBRAR = "lembrar_login"
    }
}
