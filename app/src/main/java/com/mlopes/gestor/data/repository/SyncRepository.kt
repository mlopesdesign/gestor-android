package com.mlopes.gestor.data.repository

import com.mlopes.gestor.data.local.dao.PendingOpDao
import com.mlopes.gestor.data.remote.api.SyncApi
import com.mlopes.gestor.data.remote.dto.MutacaoDto
import com.mlopes.gestor.data.remote.dto.PushRequest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Coordena o sync bidirecional com o plugin WP.
 * Pull = puxa deltas desde o cursor; Push = enfileira pending_ops.
 */
@Singleton
class SyncRepository @Inject constructor(
    private val syncApi: SyncApi,
    private val tarefaRepository: TarefaRepository,
    private val projetoRepository: ProjetoRepository,
    private val clienteRepository: ClienteRepository,
    private val areaRepository: AreaRepository,
    private val pendingOpDao: PendingOpDao,
    private val networkMonitor: NetworkMonitor,
    private val authRepository: AuthRepository,
) {
    suspend fun sincronizarTudo(): Result<Unit> = runCatching {
        if (!networkMonitor.connected()) error("Sem conexao.")
        flushPending()
        tarefaRepository.refresh()
        projetoRepository.refresh()
        clienteRepository.refresh()
        areaRepository.refresh()
    }

    suspend fun flushPending(): Result<Unit> = runCatching {
        if (!networkMonitor.connected()) return@runCatching
        val ops = pendingOpDao.listar()
        if (ops.isEmpty()) return@runCatching
        val mutacoes = ops.map { op ->
            MutacaoDto(
                tabela = op.tabela,
                operacao = op.operacao,
                registroId = op.registroId,
                payload = kotlinx.serialization.json.Json.parseToJsonElement(op.payloadJson),
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
