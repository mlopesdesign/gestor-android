package com.mlopes.gestor.di

import android.content.Context
import androidx.room.Room
import com.mlopes.gestor.data.local.dao.AreaDao
import com.mlopes.gestor.data.local.dao.ClienteDao
import com.mlopes.gestor.data.local.dao.PendingOpDao
import com.mlopes.gestor.data.local.dao.ProjetoDao
import com.mlopes.gestor.data.local.dao.TarefaDao
import com.mlopes.gestor.data.local.db.GestorDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): GestorDatabase =
        Room.databaseBuilder(context, GestorDatabase::class.java, GestorDatabase.NOME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun tarefaDao(db: GestorDatabase): TarefaDao = db.tarefaDao()
    @Provides fun projetoDao(db: GestorDatabase): ProjetoDao = db.projetoDao()
    @Provides fun clienteDao(db: GestorDatabase): ClienteDao = db.clienteDao()
    @Provides fun areaDao(db: GestorDatabase): AreaDao = db.areaDao()
    @Provides fun pendingOpDao(db: GestorDatabase): PendingOpDao = db.pendingOpDao()
}
