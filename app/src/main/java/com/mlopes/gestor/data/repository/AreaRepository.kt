package com.mlopes.gestor.data.repository

import com.mlopes.gestor.data.local.dao.AreaDao
import com.mlopes.gestor.data.remote.api.AreasApi
import com.mlopes.gestor.data.remote.dto.toDomain
import com.mlopes.gestor.data.remote.dto.toEntity
import com.mlopes.gestor.domain.model.Area
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AreaRepository @Inject constructor(
    private val api: AreasApi,
    private val dao: AreaDao,
    private val networkMonitor: NetworkMonitor,
) {
    fun observar(): Flow<List<Area>> = dao.observarTodas().map { list -> list.map { it.toDomain() } }

    suspend fun refresh(): Result<Unit> = runCatching {
        if (!networkMonitor.connected()) return@runCatching
        val response = api.listar()
        if (!response.isSuccessful) return@runCatching
        val itens = response.body()?.data?.items.orEmpty()
        dao.limpar()
        dao.inserirTodos(itens.map { it.toEntity() })
    }
}
