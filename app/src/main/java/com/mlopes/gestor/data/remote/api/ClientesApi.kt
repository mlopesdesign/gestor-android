package com.mlopes.gestor.data.remote.api

import com.mlopes.gestor.data.remote.dto.ApiEnvelope
import com.mlopes.gestor.data.remote.dto.ClienteDto
import com.mlopes.gestor.data.remote.dto.ClienteInputDto
import com.mlopes.gestor.data.remote.dto.CursorEnvelope
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ClientesApi {
    @GET("clientes")
    suspend fun listar(): Response<ApiEnvelope<CursorEnvelope<ClienteDto>>>

    @GET("clientes/{id}")
    suspend fun buscar(@Path("id") id: String): Response<ApiEnvelope<ClienteDto>>

    @POST("clientes")
    suspend fun criar(@Body body: ClienteInputDto): Response<ApiEnvelope<ClienteDto>>

    @PUT("clientes/{id}")
    suspend fun atualizar(
        @Path("id") id: String,
        @Body body: ClienteInputDto,
    ): Response<ApiEnvelope<ClienteDto>>

    @DELETE("clientes/{id}")
    suspend fun excluir(@Path("id") id: String): Response<ApiEnvelope<Unit>>
}
