package com.mlopes.gestor.data.remote.api

import com.mlopes.gestor.data.remote.dto.ApiEnvelope
import com.mlopes.gestor.data.remote.dto.CursorEnvelope
import com.mlopes.gestor.data.remote.dto.TarefaConcluidaRequest
import com.mlopes.gestor.data.remote.dto.TarefaDto
import com.mlopes.gestor.data.remote.dto.TarefaInputDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface TarefasApi {
    @GET("tarefas")
    suspend fun listar(
        @Query("since") since: String? = null,
        @Query("limit") limit: Int = 200,
        @Query("offset") offset: Int = 0,
    ): Response<ApiEnvelope<CursorEnvelope<TarefaDto>>>

    @GET("tarefas/hoje")
    suspend fun listarHoje(): Response<ApiEnvelope<CursorEnvelope<TarefaDto>>>

    @GET("tarefas/atrasadas")
    suspend fun listarAtrasadas(): Response<ApiEnvelope<CursorEnvelope<TarefaDto>>>

    @GET("tarefas/{id}")
    suspend fun buscar(@Path("id") id: String): Response<ApiEnvelope<TarefaDto>>

    @POST("tarefas")
    suspend fun criar(@Body body: TarefaInputDto): Response<ApiEnvelope<TarefaDto>>

    @PUT("tarefas/{id}")
    suspend fun atualizar(
        @Path("id") id: String,
        @Body body: TarefaInputDto,
    ): Response<ApiEnvelope<TarefaDto>>

    @DELETE("tarefas/{id}")
    suspend fun excluir(@Path("id") id: String): Response<ApiEnvelope<Unit>>

    @POST("tarefas/{id}/concluir")
    suspend fun concluir(
        @Path("id") id: String,
        @Body body: TarefaConcluidaRequest = TarefaConcluidaRequest(),
    ): Response<ApiEnvelope<TarefaDto>>

    @POST("tarefas/{id}/reabrir")
    suspend fun reabrir(@Path("id") id: String): Response<ApiEnvelope<TarefaDto>>
}
