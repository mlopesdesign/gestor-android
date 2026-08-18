package com.mlopes.gestor.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Os repositories sao injetados diretamente via @Inject constructor + @Singleton.
 * Este modulo existe para reservar espaco caso surja a necessidade de @Binds
 * (por exemplo, mock em testes).
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule
