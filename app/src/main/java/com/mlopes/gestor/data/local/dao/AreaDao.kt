package com.mlopes.gestor.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mlopes.gestor.data.local.entity.AreaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AreaDao {
    @Query("SELECT * FROM areas ORDER BY ordem ASC, nome ASC")
    fun observarTodas(): Flow<List<AreaEntity>>

    @Query("SELECT * FROM areas WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: String): AreaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirTodos(areas: List<AreaEntity>)

    @Query("DELETE FROM areas")
    suspend fun limpar()
}
