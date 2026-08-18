package com.mlopes.gestor.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mlopes.gestor.data.local.entity.ProjetoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjetoDao {
    @Query("SELECT * FROM projetos ORDER BY nome ASC")
    fun observarTodos(): Flow<List<ProjetoEntity>>

    @Query("SELECT * FROM projetos WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: String): ProjetoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirTodos(projetos: List<ProjetoEntity>)

    @Query("DELETE FROM projetos")
    suspend fun limpar()
}
