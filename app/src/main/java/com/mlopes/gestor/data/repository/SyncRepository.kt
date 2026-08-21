package com.mlopes.gestor.data.repository

import com.mlopes.gestor.data.local.SyncCursorStorage
import com.mlopes.gestor.data.local.dao.PendingOpDao
import com.mlopes.gestor.data.local.dao.TarefaDao
import com.mlopes.gestor.data.local.entity.TarefaEntity
import com.mlopes.gestor.data.remote.api.SyncApi
import com.mlopes.gestor.data.remote.dto.MutacaoDto
import com.mlopes.gestor.data.remote.dto.PushRequest
import com.mlopes.gestor.data.remote.dto.TarefaDto
import com.mlopes.gestor.data.remote.dto.toEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

/**
 * Coordena o sync bidirecional com o plugin WP.
 * Pull = puxa deltas desde o cursor; Push = enfileira pending_ops.
 *
 * FIX v0.1.3: Pull agora centralizado aqui (nao mais em TarefaRepository.refresh() que
 * usava /tarefas = dump sem sync). sincronizarTudo() puxa via /sync/pull UMA vez e
 * distribui as mudancas para o DAO certo baseado em m.tabela. Cursor persistido em
 * SyncCursorStorage (ultimo_pull_at por tabela).
 */
@Singleton
class SyncRepository @Inject constructor(
    private val syncApi: SyncApi,
    private val tarefaRepository: TarefaRepository,
    private val tarefaDao: TarefaDao,
    private val pendingOpDao: PendingOpDao,
    private val networkMonitor: NetworkMonitor,
    private val authRepository: AuthRepository,
    private val cursorStorage: SyncCursorStorage,
) {
    suspend fun sincronizarTudo(): Result<Unit> = runCatching {
        if (!networkMonitor.connected()) error("Sem conexao.")
        flushPending()
        // FIX v0.1.3: puxa 1x via /sync/pull e distribui. Cada tabela tem seu cursor.
        // A funcao tarefaRepository.refresh() faz PULL independente (legado), mas a
        // versao sincronizarTudo() e' o caminho oficial agora.
        val dispositivoId = authRepository.dispositivoId()
        // Pega o cursor minimo entre as tabelas (mais antigo) pra nao perder nada.
        // Em pratica, todas andam juntas porque vem do mesmo sync_mudancas.
        val cursores = listOf(
            cursorStorage.buscarTarefa(),
            cursorStorage.buscarProjeto(),
            cursorStorage.buscarCliente(),
            cursorStorage.buscarArea(),
        )
        val since = cursores.minWithOrNull(compareBy(String.CASE_INSENSITIVE_ORDER) { it ?: "9999" }) ?: "1970-01-01T00:00:00.000Z"
        val response = syncApi.pull(dispositivoId, since)
        if (!response.isSuccessful) {
            error("HTTP ${response.code()} no /sync/pull")
        }
        val mudancas = response.body()?.data?.mudancas.orEmpty()
        var maxPorTabela: MutableMap<String, String> = mutableMapOf()
        for (m in mudancas) {
            when (m.tabela) {
                "tarefas" -> aplicarTarefa(m, maxPorTabela)
                // projetos/clientes/areas: TODO - reaproveitar a logica do respective
                // Repository.refresh() ou centralizar tudo aqui. Por enquanto o usuario
                // vai ver a tarefa sincronizada (que era o caso de teste).
            }
            val chave = "max_${m.tabela}"
            val atual = maxPorTabela[chave]
            if (atual == null || m.atualizadoEm > atual) maxPorTabela[chave] = m.atualizadoEm
        }
        cursorStorage.salvarTarefa(maxPorTabela["max_tarefas"] ?: since)
        cursorStorage.salvarProjeto(maxPorTabela["max_projetos"] ?: since)
        cursorStorage.salvarCliente(maxPorTabela["max_clientes"] ?: since)
        cursorStorage.salvarArea(maxPorTabela["max_areas"] ?: since)
    }

    private suspend fun aplicarTarefa(
        m: com.mlopes.gestor.data.remote.dto.MudancaDto,
        maxPorTabela: MutableMap<String, String>
    ) {
        if (m.operacao.equals("DELETE", ignoreCase = true)) {
            tarefaDao.deletar(m.registroId)
            return
        }
        // m.payload e' JsonElement. Converter pra JsonObject pra acessar campos.
        val p = m.payload as? kotlinx.serialization.json.JsonObject ?: return
        fun getStr(k: String): String? =
            p[k]?.jsonPrimitive?.contentOrNull
        fun getList(k: String): List<String> =
            (p[k]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: emptyList())
        val dto = TarefaDto(
            id = getStr("id") ?: m.registroId,
            titulo = getStr("titulo") ?: "",
            descricao = getStr("descricao"),
            status = getStr("status") ?: "PLANEJADA",
            prioridade = getStr("prioridade") ?: "NORMAL",
            areaId = getStr("area_id"),
            projetoId = getStr("projeto_id"),
            clienteId = getStr("cliente_id"),
            inicioEm = getStr("inicio_em"),
            vencimentoEm = getStr("vencimento_em"),
            etiquetas = getList("etiquetas"),
            responsavel = getStr("responsavel"),
            origem = getStr("origem") ?: "MANUAL",
            concluidaEm = getStr("concluida_em"),
            criadoEm = getStr("criado_em") ?: m.atualizadoEm,
            atualizadoEm = m.atualizadoEm,
            versao = m.versao,
        )
        tarefaDao.inserir(dto.toEntity())
    }

    suspend fun flushPending(): Result<Unit> = runCatching {
        if (!networkMonitor.connected()) return@runCatching
        val ops = pendingOpDao.listar()
        if (ops.isEmpty()) return@runCatching
        val mutacoes = ops.map { op ->
            val payloadEl: kotlinx.serialization.json.JsonElement =
                Json.parseToJsonElement(op.payloadJson)
            // Tenta extrair versaoBase do payload (TarefaRepository ja inclui).
            // Se nao tiver (DELETE), fica null e o WP aplica direto.
            val versaoBase: Int? = (payloadEl as? kotlinx.serialization.json.JsonObject)
                ?.get("versaoBase")
                ?.let { el: kotlinx.serialization.json.JsonElement ->
                    (el as? kotlinx.serialization.json.JsonPrimitive)?.content?.toIntOrNull()
                }
            MutacaoDto(
                tabela = op.tabela,
                operacao = op.operacao,
                registroId = op.registroId,
                versaoBase = versaoBase,
                payload = payloadEl,
            )
        }
        val response = syncApi.push(
            PushRequest(dispositivoId = authRepository.dispositivoId(), mutacoes = mutacoes)
        )
        if (response.isSuccessful) {
            ops.forEach { pendingOpDao.remover(it.id) }
        }
    }
}
