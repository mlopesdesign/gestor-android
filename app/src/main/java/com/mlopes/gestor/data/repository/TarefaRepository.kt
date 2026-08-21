package com.mlopes.gestor.data.repository

import com.mlopes.gestor.data.local.SyncCursorStorage
import com.mlopes.gestor.data.local.dao.PendingOpDao
import com.mlopes.gestor.data.local.dao.TarefaDao
import com.mlopes.gestor.data.local.entity.PendingOpEntity
import com.mlopes.gestor.data.local.entity.TarefaEntity
import com.mlopes.gestor.data.remote.api.SyncApi
import com.mlopes.gestor.data.remote.api.TarefasApi
import com.mlopes.gestor.data.remote.dto.TarefaDto
import com.mlopes.gestor.data.remote.dto.toDomain
import com.mlopes.gestor.data.remote.dto.toEntity
import com.mlopes.gestor.domain.model.StatusTarefa
import com.mlopes.gestor.domain.model.Tarefa
import com.mlopes.gestor.domain.usecase.TarefaInput
import com.mlopes.gestor.util.Ulid
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio offline-first. Le sempre do Room. Quando online, faz refresh via /sync/pull.
 * Quando uma mutation e feita offline, enfileira em pending_ops para flush posterior.
 *
 * FIX v0.1.3: 5 bugs do sync que faziam Android nao ver mutacoes do desktop:
 *  1. refresh() agora chama /sync/pull (com cursor) em vez de /tarefas (dump sem sync)
 *  2. payload do push agora em snake_case (area_id, projeto_id, criado_em, etc) — WP rejeitava camelCase
 *  3. campos renomeados: criadaEm → criadoEm, atualizadaEm → atualizadoEm (bater com WP snake_case)
 *  4. deviceId estavel (AuthRepository.dispositivoId() persistido em SharedPreferences)
 *  5. cursor de pull persistido em SyncCursorStorage (ultimo_pull_at por tabela)
 */
@Singleton
class TarefaRepository @Inject constructor(
    private val api: TarefasApi,
    private val syncApi: SyncApi,
    private val dao: TarefaDao,
    private val pendingOpDao: PendingOpDao,
    private val networkMonitor: NetworkMonitor,
    private val authRepository: AuthRepository,
    private val cursorStorage: SyncCursorStorage,
) {
    fun observarTarefas(status: StatusTarefa? = null): Flow<List<Tarefa>> =
        if (status == null) dao.observarTodas().map { list -> list.map { it.toDomain() } }
        else dao.observarPorStatus(status.name).map { list -> list.map { it.toDomain() } }

    fun observarPendentes(): Flow<Int> = dao.observarPendentes()

    suspend fun refresh(): Result<Unit> = runCatching {
        if (!networkMonitor.connected()) return@runCatching
        // FIX v0.1.3: usar /sync/pull com cursor ao inves de /tarefas (dump).
        // O endpoint /sync/pull retorna deltas desde `since` (ISO 8601).
        // `atualizado_em` de cada mudanca vira o proximo cursor.
        val dispositivoId = authRepository.dispositivoId()
        val since = cursorStorage.buscarTarefa() ?: "1970-01-01T00:00:00.000Z"
        val response = syncApi.pull(dispositivoId, since)
        if (!response.isSuccessful) {
            error("HTTP ${response.code()} no /sync/pull")
        }
        val mudancas: List<com.mlopes.gestor.data.remote.dto.MudancaDto> =
            response.body()?.data?.mudancas.orEmpty()
        // Aplica cada mudanca incrementalmente. UPSERT por item (REPLACE strategy).
        // NAO fazer wipe destrutivo - dados offline permanecem.
        var maxAtualizadoEm: String? = null
        for (m in mudancas) {
            if (m.tabela != "tarefas") continue
            if (m.operacao.equals("DELETE", ignoreCase = true)) {
                dao.deletar(m.registroId)
            } else {
                // m.payload e' JsonElement. Converter pra JsonObject pra acessar campos.
                val p = m.payload as? kotlinx.serialization.json.JsonObject ?: continue
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
                    concluidaEm = m.payload["concluida_em"]?.jsonPrimitive?.contentOrNull,
                    criadoEm = m.payload["criado_em"]?.jsonPrimitive?.content ?: since,
                    atualizadoEm = m.atualizadoEm,
                    versao = m.versao,
                )
                dao.inserir(dto.toEntity())
            }
            if (maxAtualizadoEm == null || m.atualizadoEm > maxAtualizadoEm) {
                maxAtualizadoEm = m.atualizadoEm
            }
        }
        if (maxAtualizadoEm != null) cursorStorage.salvarTarefa(maxAtualizadoEm)
    }

    suspend fun buscarPorId(id: String): Result<Tarefa> = runCatching {
        dao.buscarPorId(id)?.let { return@runCatching it.toDomain() }
        val response = api.buscar(id)
        if (!response.isSuccessful) error("Tarefa nao encontrada.")
        val dto = response.body()?.data ?: error("Resposta vazia.")
        dao.inserir(dto.toEntity())
        dto.toDomain()
    }

    suspend fun criar(input: TarefaInput): Result<Tarefa> = runCatching {
        val agora = Instant.now().toString()
        val idLocal = Ulid.next()
        val entity = TarefaEntity(
            id = idLocal,
            titulo = input.titulo,
            descricao = input.descricao,
            status = input.status.name,
            prioridade = input.prioridade.name,
            projetoId = input.projetoId,
            clienteId = input.clienteId,
            areaId = null,
            vencimentoEm = input.vencimentoEm,
            etiquetas = "",
            responsavel = null,
            concluidaEm = null,
            criadoEm = agora,
            atualizadoEm = agora,
            versao = 1,
            pendenteSync = true,
        )
        dao.inserir(entity)
        pendingOpDao.enfileirar(
            PendingOpEntity(
                tabela = "tarefas",
                operacao = "UPSERT",
                registroId = idLocal,
                payloadJson = entity.toSyncJson(versaoBase = 0),
                criadoEm = agora,
            )
        )
        entity.toDomain()
    }

    suspend fun atualizar(id: String, input: TarefaInput): Result<Tarefa> = runCatching {
        val atual = dao.buscarPorId(id) ?: error("Tarefa nao encontrada.")
        val agora = Instant.now().toString()
        val versaoBase = atual.versao
        val entity = atual.copy(
            titulo = input.titulo,
            descricao = input.descricao,
            status = input.status.name,
            prioridade = input.prioridade.name,
            projetoId = input.projetoId,
            clienteId = input.clienteId,
            vencimentoEm = input.vencimentoEm,
            atualizadoEm = agora,
            versao = atual.versao + 1,
            pendenteSync = true,
        )
        dao.inserir(entity)
        pendingOpDao.enfileirar(
            PendingOpEntity(
                tabela = "tarefas",
                operacao = "UPSERT",
                registroId = id,
                payloadJson = entity.toSyncJson(versaoBase = versaoBase),
                criadoEm = agora,
            )
        )
        entity.toDomain()
    }

    suspend fun concluir(id: String): Result<Unit> = runCatching {
        val agora = Instant.now().toString()
        val atual = dao.buscarPorId(id) ?: return@runCatching
        val versaoBase = atual.versao
        val entity = atual.copy(
            status = StatusTarefa.CONCLUIDA.name,
            concluidaEm = agora,
            atualizadoEm = agora,
            versao = atual.versao + 1,
            pendenteSync = true,
        )
        dao.inserir(entity)
        pendingOpDao.enfileirar(
            PendingOpEntity(
                tabela = "tarefas",
                operacao = "UPSERT",
                registroId = id,
                payloadJson = entity.toSyncJson(versaoBase = versaoBase),
                criadoEm = agora,
            )
        )
    }

    suspend fun reabrir(id: String): Result<Unit> = runCatching {
        val agora = Instant.now().toString()
        val atual = dao.buscarPorId(id) ?: return@runCatching
        val versaoBase = atual.versao
        val entity = atual.copy(
            status = StatusTarefa.PLANEJADA.name,
            concluidaEm = null,
            atualizadoEm = agora,
            versao = atual.versao + 1,
            pendenteSync = true,
        )
        dao.inserir(entity)
        pendingOpDao.enfileirar(
            PendingOpEntity(
                tabela = "tarefas",
                operacao = "UPSERT",
                registroId = id,
                payloadJson = entity.toSyncJson(versaoBase = versaoBase),
                criadoEm = agora,
            )
        )
    }

    suspend fun excluir(id: String): Result<Unit> = runCatching {
        val agora = Instant.now().toString()
        dao.deletar(id)
        pendingOpDao.enfileirar(
            PendingOpEntity(
                tabela = "tarefas",
                operacao = "DELETE",
                registroId = id,
                payloadJson = """{"id":"$id"}""",
                criadoEm = agora,
            )
        )
    }
}

/**
 * FIX v0.1.3: gera JSON de sync em SNAKE_CASE, que e' o que o WP espera.
 * Antes vinha camelCase (projetoId, clienteId, criadoEm, etc) e o WP rejeitava
 * silenciosamente - campos viravam null. Agora vai com projeto_id, cliente_id,
 * criado_em, atualizado_em, etc, batendo com Tarefa::upsert (class-tarefa.php:39-122).
 *
 * O versaoBase e a versao que o cliente tinha ANTES da modificacao (OT-locking).
 */
private fun TarefaEntity.toSyncJson(versaoBase: Int): String = buildString {
    append('{')
    append("\"id\":\"").append(id).append("\",")
    append("\"titulo\":\"").append(titulo.replace("\\", "\\\\").replace("\"", "\\\"")).append("\",")
    append("\"descricao\":").append(descricao?.let { "\"${it.replace("\\", "\\\\").replace("\"", "\\\"")}\"" } ?: "null").append(",")
    append("\"status\":\"").append(status).append("\",")
    append("\"prioridade\":\"").append(prioridade).append("\",")
    append("\"nivel_cobranca\":\"PERSISTENTE\",")
    append("\"origem\":\"ANDROID\",")
    append("\"projeto_id\":").append(projetoId?.let { "\"$it\"" } ?: "null").append(",")
    append("\"cliente_id\":").append(clienteId?.let { "\"$it\"" } ?: "null").append(",")
    append("\"area_id\":").append(areaId?.let { "\"$it\"" } ?: "null").append(",")
    append("\"vencimento_em\":").append(vencimentoEm?.let { "\"$it\"" } ?: "null").append(",")
    append("\"etiquetas\":[],")
    append("\"responsavel\":").append(responsavel?.let { "\"$it\"" } ?: "null").append(",")
    append("\"concluida_em\":").append(concluidaEm?.let { "\"$it\"" } ?: "null").append(",")
    append("\"criado_em\":\"").append(criadoEm).append("\",")
    append("\"atualizado_em\":\"").append(atualizadoEm).append("\",")
    append("\"versao\":").append(versao).append(",")
    append("\"versao_base\":").append(versaoBase)
    append('}')
}
