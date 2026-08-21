package com.mlopes.gestor.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FIX v0.1.3: persistir o cursor do /sync/pull (ultimo `atualizado_em` recebido do WP)
 * por tabela. Antes nao havia cursor: o refresh sempre trazia tudo e o wipe destrutivo
 * do TarefaDao.substituir() apagava o que ja existia local. Agora o cursor fica em
 * SharedPreferences (nao precisa criptografar - e' um timestamp publico do proprio usuario).
 *
 * Formato do cursor: ISO 8601 UTC (ex: "2026-08-20T23:34:47.000Z").
 * O WP aceita ISO 8601 e converte pra MySQL DATETIME no WHERE.
 *
 * Se nunca puxou, retorna null e o Repository usa "1970-01-01T00:00:00.000Z"
 * (pega tudo desde o inicio).
 */
@Singleton
class SyncCursorStorage @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("gestor_sync_cursor", Context.MODE_PRIVATE)

    fun buscar(tabela: String): String? = prefs.getString(key(tabela), null)

    fun salvar(tabela: String, iso: String) {
        prefs.edit().putString(key(tabela), iso).apply()
    }

    // Atalhos por tabela (mais faceis de usar sem errar nome).
    fun buscarTarefa(): String? = buscar(KEY_TAREFAS)
    fun salvarTarefa(iso: String) = salvar(KEY_TAREFAS, iso)
    fun buscarProjeto(): String? = buscar(KEY_PROJETOS)
    fun salvarProjeto(iso: String) = salvar(KEY_PROJETOS, iso)
    fun buscarCliente(): String? = buscar(KEY_CLIENTES)
    fun salvarCliente(iso: String) = salvar(KEY_CLIENTES, iso)
    fun buscarArea(): String? = buscar(KEY_AREAS)
    fun salvarArea(iso: String) = salvar(KEY_AREAS, iso)

    fun limpar() {
        prefs.edit().clear().apply()
    }

    private fun key(tabela: String) = "ultimo_pull_at_$tabela"

    companion object {
        private const val KEY_TAREFAS = "tarefas"
        private const val KEY_PROJETOS = "projetos"
        private const val KEY_CLIENTES = "clientes"
        private const val KEY_AREAS = "areas"
    }
}
