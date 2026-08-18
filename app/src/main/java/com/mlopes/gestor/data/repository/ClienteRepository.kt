package com.mlopes.gestor.data.repository

import com.mlopes.gestor.data.local.dao.ClienteDao
import com.mlopes.gestor.data.remote.api.ClientesApi
import com.mlopes.gestor.data.remote.dto.toDomain
import com.mlopes.gestor.data.remote.dto.toEntity
import com.mlopes.gestor.domain.model.Cliente
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClienteRepository @Inject constructor(
    private val api: ClientesApi,
    private val dao: ClienteDao,
    private val networkMonitor: NetworkMonitor,
) {
    fun observar(): Flow<List<Cliente>> = dao.observarTodos().map { list -> list.map { it.toDomain() } }

    fun buscar(texto: String): Flow<List<Cliente>> = dao.buscar(texto).map { list -> list.map { it.toDomain() } }

    suspend fun refresh(): Result<Unit> = runCatching {
        if (!networkMonitor.connected()) return@runCatching
        val response = api.listar()
        if (!response.isSuccessful) return@runCatching
        val itens = response.body()?.data?.items.orEmpty()
        dao.limpar()
        dao.inserirTodos(itens.map { it.toEntity() })
    }
}
