package com.mlopes.gestor.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Configura o logging do OkHttp. Nivel BODY em debug, NONE em release.
 */
@Singleton
class LoggingInterceptor @Inject constructor() {
    fun build(): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
}
