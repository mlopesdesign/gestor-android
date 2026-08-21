package com.mlopes.gestor.data.repository

import com.mlopes.gestor.data.local.SyncCursorStorage
import com.mlopes.gestor.data.local.dao.AreaDao
import com.mlopes.gestor.data.local.dao.ClienteDao
import com.mlopes.gestor.data.local.dao.PendingOpDao
import com.mlopes.gestor.data.local.dao.ProjetoDao
import com.mlopes.gestor.data.local.dao.TarefaDao
import com.mlopes.gestor.data.remote.api.SyncApi
import com.mlopes.gestor.data.remote.dto.MutacaoDto
import com.mlopes.gestor.data.remote.dto.PushRequest
import com.mlopes.gestor.data.remote.dto.TarefaDto
import com.mlopes.gestor.data.remote.dto.toEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

/**
 * Coordena o sync bidirecional com o plugin WP.
 * Pull = puxa deltas desde o cursor; Push = enfileira pending_ops.
 *
 * v0.1.5: agora processa TODAS as tabelas do payload (areas, clientes,
 * projetos, tarefas). Antes so' tarefas era aplicado — areas/clientes/
 * projetos vinham no payload do /sync/pull mas eram descartados, entao a
 * UI de Areas/Clientes/Projetos ficava vazia. Tambem reseta pendenteSync
 * quando o PULL confirma que a tarefa existe no servidor.
 */
@Singleton
class SyncRepository @Inject constructor(
    private val syncApi: SyncApi,
    private val tarefaRepository: TarefaRepository,
    private val tarefaDao: TarefaDao,
    private val areaDao: AreaDao,
    private val clienteDao: ClienteDao,
    private val projetoDao: ProjetoDao,
    private val pendingOpDao: PendingOpDao,
    private val networkMonitor: NetworkMonitor,
    private val authRepository: AuthRepository,
    private val cursorStorage: SyncCursorStorage,
) {
    suspend fun sincronizarTudo(): Result<Unit> = runCatching {
        if (!networkMonitor.connected()) error("Sem conexao.")
        flushPending()
        val dispositivoId = authRepository.dispositivoId()
        val cursores = listOf(
            cursorStorage.buscarTarefa(),
            cursorStorage.buscarProjeto(),
            cursorStorage.buscarCliente(),
            cursorStorage.buscarArea(),
        )
        val since = cursores.minWithOrNull(
            compareBy(String.CASE_INSENSITIVE_ORDER) { it ?: "9999" }
        ) ?: "1970-01-01T00:00:00.000Z"
        val response = syncApi.pull(dispositivoId, since)
        if (!response.isSuccessful) {
            error("HTTP ${response.code()} no /sync/pull")
        }
        val mudancas = response.body()?.data?.mudancas.orEmpty()
        val maxPorTabela: MutableMap<String, String> = mutableMapOf()
        for (m in mudancas) {
            when (m.tabela) {
                "tarefas" -> aplicarTarefa(m, maxPorTabela)
                "areas" -> aplicarArea(m, maxPorTabela)
                "clientes" -> aplicarCliente(m, maxPorTabela)
                "projetos" -> aplicarProjeto(m, maxPorTabela)
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
        @Suppress("UNUSED_PARAMETER") maxPorTabela: MutableMap<String, String>
    ) {
        if (m.operacao.equals("DELETE", ignoreCase = true)) {
            tarefaDao.deletar(m.registroId)
            return
        }
        val p = m.payload as? JsonObject ?: return
        fun getStr(k: String): String? = p[k]?.jsonPrimitive?.contentOrNull
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
        // toEntity() ja seta pendenteSync=false (estado vindo do servidor).
        // Se a tarefa local tinha pendenteSync=true e o servidor retornou
        // ela de volta, significa que o PUSH foi aplicado com sucesso e
        // a versao local agora pode ser limpa.
        tarefaDao.inserir(dto.toEntity())
    }

    private suspend fun aplicarArea(
        m: com.mlopes.gestor.data.remote.dto.MudancaDto,
        @Suppress("UNUSED_PARAMETER") maxPorTabela: MutableMap<String, String>
    ) {
        if (m.operacao.equals("DELETE", ignoreCase = true)) {
            areaDao.limpar() // sem DAO.deleteById, melhor limpar tudo (raro)
            return
        }
        val p = m.payload as? JsonObject ?: return
        val entity = com.mlopes.gestor.data.local.entity.AreaEntity(
            id = m.registroId,
            nome = p["nome"]?.jsonPrimitive?.contentOrNull ?: "",
            cor = p["cor"]?.jsonPrimitive?.contentOrNull,
            icone = p["icone"]?.jsonPrimitive?.contentOrNull,
            ordem = p["ordem"]?.jsonPrimitive?.contentOrNull?.toIntOrNull() ?: 0,
            criadoEm = p["criado_em"]?.jsonPrimitive?.contentOrNull ?: m.atualizadoEm,
        )
        // REPLACE por id (sem wipe destrutivo)
        areaDao.inserirTodos(listOf(entity))
    }

    private suspend fun aplicarCliente(
        m: com.mlopes.gestor.data.remote.dto.MudancaDto,
        @Suppress("UNUSED_PARAMETER") maxPorTabela: MutableMap<String, String>
    ) {
        if (m.operacao.equals("DELETE", ignoreCase = true)) {
            clienteDao.limpar()
            return
        }
        val p = m.payload as? JsonObject ?: return
        val entity = com.mlopes.gestor.data.local.entity.ClienteEntity(
            id = m.registroId,
            nome = p["nome"]?.jsonPrimitive?.contentOrNull ?: "",
            email = p["email"]?.jsonPrimitive?.contentOrNull,
            telefone = p["telefone"]?.jsonPrimitive?.contentOrNull,
            observacoes = p["observacoes"]?.jsonPrimitive?.contentOrNull,
            ativo = p["ativo"]?.jsonPrimitive?.contentOrNull?.toBooleanStrictOrNull() ?: true,
            criadoEm = p["criado_em"]?.jsonPrimitive?.contentOrNull ?: m.atualizadoEm,
            atualizadoEm = m.atualizadoEm,
        )
        clienteDao.inserirTodos(listOf(entity))
    }

    private suspend fun aplicarProjeto(
        m: com.mlopes.gestor.data.remote.dto.MudancaDto,
        @Suppress("UNUSED_PARAMETER") maxPorTabela: MutableMap<String, String>
    ) {
        if (m.operacao.equals("DELETE", ignoreCase = true)) {
            projetoDao.limpar()
            return
        }
        val p = m.payload as? JsonObject ?: return
        val entity = com.mlopes.gestor.data.local.entity.ProjetoEntity(
            id = m.registroId,
            nome = p["nome"]?.jsonPrimitive?.contentOrNull ?: "",
            descricao = p["descricao"]?.jsonPrimitive?.contentOrNull,
            status = p["status"]?.jsonPrimitive?.contentOrNull ?: "PLANEJADO",
            clienteId = p["cliente_id"]?.jsonPrimitive?.contentOrNull,
            areaId = p["area_id"]?.jsonPrimitive?.contentOrNull,
            criadoEm = p["criado_em"]?.jsonPrimitive?.contentOrNull ?: m.atualizadoEm,
            atualizadoEm = m.atualizadoEm,
            versao = m.versao,
        )
        projetoDao.inserirTodos(listOf(entity))
    }

    suspend fun flushPending(): Result<Unit> = runCatching {
        if (!networkMonitor.connected()) return@runCatching
        val ops = pendingOpDao.listar()
        if (ops.isEmpty()) return@runCatching
        val mutacoes = ops.map { op ->
            val payloadEl: kotlinx.serialization.json.JsonElement =
                Json.parseToJsonElement(op.payloadJson)
            val versaoBase: Int? = (payloadEl as? JsonObject)
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
