package com.mlopes.gestor.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.mlopes.gestor.data.local.entity.TarefaEntity
import com.mlopes.gestor.domain.model.StatusTarefa
import kotlinx.coroutines.flow.Flow

@Dao
interface TarefaDao {
    @Query("SELECT * FROM tarefas WHERE status != 'CONCLUIDA' OR :incluirConcluidas = 1 ORDER BY vencimentoEm ASC")
    fun observarTodas(incluirConcluidas: Boolean = true): Flow<List<TarefaEntity>>

    @Query("SELECT * FROM tarefas WHERE status = :status ORDER BY vencimentoEm ASC")
    fun observarPorStatus(status: String): Flow<List<TarefaEntity>>

    @Query("SELECT * FROM tarefas WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: String): TarefaEntity?

    @Query("SELECT COUNT(*) FROM tarefas WHERE status != 'CONCLUIDA'")
    fun observarPendentes(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(tarefa: TarefaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirTodas(tarefas: List<TarefaEntity>)

    @Query("DELETE FROM tarefas WHERE id = :id")
    suspend fun deletar(id: String)

    @Query("DELETE FROM tarefas")
    suspend fun limpar()

    @Transaction
    suspend fun substituir(tarefas: List<TarefaEntity>) {
        // FIX v0.1.3: NAO fazer wipe destrutivo. /sync/pull retorna apenas DELTAS
        // desde o cursor; se apagarmos tudo a cada refresh, perderiamos dados offline
        // que ainda nao tinham sido sincronizados (fila pending_ops). Agora: UPSERT
        // por item (REPLACE strategy do inserir). Dados offline permanecem ate a
        // pending_op subir.
        for (t in tarefas) inserir(t)
    }
}
