package com.mlopes.gestor.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mlopes.gestor.data.local.dao.AreaDao
import com.mlopes.gestor.data.local.dao.ClienteDao
import com.mlopes.gestor.data.local.dao.PendingOpDao
import com.mlopes.gestor.data.local.dao.ProjetoDao
import com.mlopes.gestor.data.local.dao.TarefaDao
import com.mlopes.gestor.data.local.entity.AreaEntity
import com.mlopes.gestor.data.local.entity.ClienteEntity
import com.mlopes.gestor.data.local.entity.PendingOpEntity
import com.mlopes.gestor.data.local.entity.ProjetoEntity
import com.mlopes.gestor.data.local.entity.TarefaEntity

@Database(
    entities = [
        TarefaEntity::class,
        ProjetoEntity::class,
        ClienteEntity::class,
        AreaEntity::class,
        PendingOpEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class GestorDatabase : RoomDatabase() {
    abstract fun tarefaDao(): TarefaDao
    abstract fun projetoDao(): ProjetoDao
    abstract fun clienteDao(): ClienteDao
    abstract fun areaDao(): AreaDao
    abstract fun pendingOpDao(): PendingOpDao

    companion object {
        const val NOME = "gestor.db"
    }
}
