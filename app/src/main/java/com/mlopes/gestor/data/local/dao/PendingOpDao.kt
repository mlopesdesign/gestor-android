package com.mlopes.gestor.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mlopes.gestor.data.local.entity.PendingOpEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingOpDao {
    @Query("SELECT * FROM pending_ops ORDER BY criadoEm ASC")
    fun observar(): Flow<List<PendingOpEntity>>

    @Query("SELECT * FROM pending_ops ORDER BY criadoEm ASC")
    suspend fun listar(): List<PendingOpEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun enfileirar(op: PendingOpEntity): Long

    @Query("DELETE FROM pending_ops WHERE id = :id")
    suspend fun remover(id: Long)

    @Query("DELETE FROM pending_ops")
    suspend fun limpar()

    @Query("UPDATE pending_ops SET tentativas = tentativas + 1, ultimoErro = :erro WHERE id = :id")
    suspend fun registrarFalha(id: Long, erro: String)
}
