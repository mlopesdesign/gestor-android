package com.mlopes.gestor.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mlopes.gestor.data.local.entity.ClienteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClienteDao {
    @Query("SELECT * FROM clientes ORDER BY nome ASC")
    fun observarTodos(): Flow<List<ClienteEntity>>

    @Query("SELECT * FROM clientes WHERE nome LIKE '%' || :q || '%' ORDER BY nome ASC")
    fun buscar(q: String): Flow<List<ClienteEntity>>

    @Query("SELECT * FROM clientes WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: String): ClienteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirTodos(clientes: List<ClienteEntity>)

    @Query("DELETE FROM clientes")
    suspend fun limpar()
}
