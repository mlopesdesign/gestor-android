package com.mlopes.gestor.di

import com.mlopes.gestor.BuildConfig
import com.mlopes.gestor.data.remote.api.AreasApi
import com.mlopes.gestor.data.remote.api.AuthApi
import com.mlopes.gestor.data.remote.api.ClientesApi
import com.mlopes.gestor.data.remote.api.ProjetosApi
import com.mlopes.gestor.data.remote.api.SyncApi
import com.mlopes.gestor.data.remote.api.TarefasApi
import com.mlopes.gestor.data.remote.interceptor.AuthInterceptor
import com.mlopes.gestor.data.remote.interceptor.LoggingInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun json(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun okHttpClient(
        authInterceptor: AuthInterceptor,
        loggingInterceptor: LoggingInterceptor,
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor.build())
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun retrofit(client: OkHttpClient, json: Json): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun authApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun tarefasApi(retrofit: Retrofit): TarefasApi = retrofit.create(TarefasApi::class.java)

    @Provides
    @Singleton
    fun projetosApi(retrofit: Retrofit): ProjetosApi = retrofit.create(ProjetosApi::class.java)

    @Provides
    @Singleton
    fun clientesApi(retrofit: Retrofit): ClientesApi = retrofit.create(ClientesApi::class.java)

    @Provides
    @Singleton
    fun areasApi(retrofit: Retrofit): AreasApi = retrofit.create(AreasApi::class.java)

    @Provides
    @Singleton
    fun syncApi(retrofit: Retrofit): SyncApi = retrofit.create(SyncApi::class.java)
}
