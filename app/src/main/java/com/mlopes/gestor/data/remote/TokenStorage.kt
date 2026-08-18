package com.mlopes.gestor.data.remote

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Armazena o token de sessao e a data de expiracao em SharedPreferences criptografado.
 * Vide AGENTS.md §6.4.
 */
@Singleton
class TokenStorage @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "gestor_secure",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    fun salvar(token: String, expiraEm: Instant) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_EXP, expiraEm.toString())
            .apply()
    }

    fun buscar(): String? = prefs.getString(KEY_TOKEN, null)

    fun expirou(): Boolean {
        val str = prefs.getString(KEY_EXP, null) ?: return true
        return runCatching { Instant.parse(str).isBefore(Instant.now()) }.getOrDefault(true)
    }

    fun limpar() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_TOKEN = "token"
        private const val KEY_EXP = "expira_em"
    }
}
