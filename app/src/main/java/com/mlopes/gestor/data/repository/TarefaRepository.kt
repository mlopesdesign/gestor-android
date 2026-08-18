package com.mlopes.gestor.data.repository

import com.mlopes.gestor.data.local.dao.PendingOpDao
import com.mlopes.gestor.data.local.dao.TarefaDao
import com.mlopes.gestor.data.local.entity.PendingOpEntity
import com.mlopes.gestor.data.local.entity.TarefaEntity
import com.mlopes.gestor.data.remote.api.TarefasApi
import com.mlopes.gestor.data.remote.dto.TarefaDto
import com.mlopes.gestor.data.remote.dto.TarefaInputDto
import com.mlopes.gestor.data.remote.dto.toDomain
import com.mlopes.gestor.data.remote.dto.toEntity
import com.mlopes.gestor.domain.model.StatusTarefa
import com.mlopes.gestor.domain.model.Tarefa
import com.mlopes.gestor.domain.usecase.TarefaInput
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio offline-first. Le sempre do Room. Quando online, faz refresh da API.
 * Quando uma mutation e feita offline, enfileira em pending_ops para flush posterior.
 */
@Singleton
class TarefaRepository @Inject constructor(
    private val api: TarefasApi,
    private val dao: TarefaDao,
    private val pendingOpDao: PendingOpDao,
    private val networkMonitor: NetworkMonitor,
) {
    fun observarTarefas(status: StatusTarefa? = null): Flow<List<Tarefa>> =
        if (status == null) dao.observarTodas().map { list -> list.map { it.toDomain() } }
        else dao.observarPorStatus(status.name).map { list -> list.map { it.toDomain() } }

    fun observarPendentes(): Flow<Int> = dao.observarPendentes()

    suspend fun refresh(): Result<Unit> = runCatching {
        if (!networkMonitor.connected()) return@runCatching
        val response = api.listar()
        if (!response.isSuccessful) return@runCatching
        val itens: List<TarefaDto> = response.body()?.data?.items.orEmpty()
        dao.substituir(itens.map { it.toEntity() })
    }

    suspend fun buscarPorId(id: String): Result<Tarefa> = runCatching {
        // 1) tenta local
        dao.buscarPorId(id)?.let { return@runCatching it.toDomain() }
        // 2) tenta remoto
        val response = api.buscar(id)
        if (!response.isSuccessful) error("Tarefa nao encontrada.")
        val dto = response.body()?.data ?: error("Resposta vazia.")
        dao.inserir(dto.toEntity())
        dto.toDomain()
    }

    suspend fun criar(input: TarefaInput): Result<Tarefa> = runCatching {
        val agora = Instant.now().toString()
        val idLocal = "local-${UUID.randomUUID()}"
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
            criadaEm = agora,
            atualizadaEm = agora,
            versao = 1,
            pendenteSync = true,
        )
        dao.inserir(entity)
        pendingOpDao.enfileirar(
            PendingOpEntity(
                tabela = "tarefas",
                operacao = "UPSERT",
                registroId = idLocal,
                payloadJson = input.toJson(),
                criadaEm = agora,
            )
        )
        entity.toDomain()
    }

    suspend fun atualizar(id: String, input: TarefaInput): Result<Tarefa> = runCatching {
        val atual = dao.buscarPorId(id) ?: error("Tarefa nao encontrada.")
        val agora = Instant.now().toString()
        val entity = atual.copy(
            titulo = input.titulo,
            descricao = input.descricao,
            status = input.status.name,
            prioridade = input.prioridade.name,
            projetoId = input.projetoId,
            clienteId = input.clienteId,
            vencimentoEm = input.vencimentoEm,
            atualizadaEm = agora,
            versao = atual.versao + 1,
            pendenteSync = true,
        )
        dao.inserir(entity)
        pendingOpDao.enfileirar(
            PendingOpEntity(
                tabela = "tarefas",
                operacao = "UPSERT",
                registroId = id,
                payloadJson = input.copy().toJson(),
                criadaEm = agora,
            )
        )
        entity.toDomain()
    }

    suspend fun concluir(id: String): Result<Unit> = runCatching {
        val agora = Instant.now().toString()
        val atual = dao.buscarPorId(id) ?: return@runCatching
        dao.inserir(
            atual.copy(
                status = StatusTarefa.CONCLUIDA.name,
                concluidaEm = agora,
                atualizadaEm = agora,
                versao = atual.versao + 1,
                pendenteSync = true,
            )
        )
        pendingOpDao.enfileirar(
            PendingOpEntity(
                tabela = "tarefas",
                operacao = "CONCLUIR",
                registroId = id,
                payloadJson = """{"id":"$id"}""",
                criadaEm = agora,
            )
        )
    }

    suspend fun reabrir(id: String): Result<Unit> = runCatching {
        val agora = Instant.now().toString()
        val atual = dao.buscarPorId(id) ?: return@runCatching
        dao.inserir(
            atual.copy(
                status = StatusTarefa.PENDENTE.name,
                concluidaEm = null,
                atualizadaEm = agora,
                versao = atual.versao + 1,
                pendenteSync = true,
            )
        )
    }

    suspend fun excluir(id: String): Result<Unit> = runCatching {
        dao.deletar(id)
        pendingOpDao.enfileirar(
            PendingOpEntity(
                tabela = "tarefas",
                operacao = "DELETE",
                registroId = id,
                payloadJson = """{"id":"$id"}""",
                criadaEm = Instant.now().toString(),
            )
        )
    }
}

private fun TarefaInput.toJson(): String = buildString {
    append('{')
    append("\"titulo\":\"").append(titulo.replace("\"", "\\\"")).append("\",")
    append("\"descricao\":").append(descricao?.let { "\"${it.replace("\"", "\\\"")}\"" } ?: "null").append(",")
    append("\"status\":\"").append(status.name).append("\",")
    append("\"prioridade\":\"").append(prioridade.name).append("\",")
    append("\"projetoId\":").append(projetoId?.let { "\"$it\"" } ?: "null").append(",")
    append("\"clienteId\":").append(clienteId?.let { "\"$it\"" } ?: "null").append(",")
    append("\"vencimentoEm\":").append(vencimentoEm?.let { "\"$it\"" } ?: "null")
    append('}')
}
