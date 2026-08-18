package com.mlopes.gestor.data.remote.api

import com.mlopes.gestor.data.remote.dto.ApiEnvelope
import com.mlopes.gestor.data.remote.dto.AreaDto
import com.mlopes.gestor.data.remote.dto.AreaInputDto
import com.mlopes.gestor.data.remote.dto.CursorEnvelope
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AreasApi {
    @GET("areas")
    suspend fun listar(): Response<ApiEnvelope<CursorEnvelope<AreaDto>>>

    @GET("areas/{id}")
    suspend fun buscar(@Path("id") id: String): Response<ApiEnvelope<AreaDto>>

    @POST("areas")
    suspend fun criar(@Body body: AreaInputDto): Response<ApiEnvelope<AreaDto>>

    @PUT("areas/{id}")
    suspend fun atualizar(
        @Path("id") id: String,
        @Body body: AreaInputDto,
    ): Response<ApiEnvelope<AreaDto>>

    @DELETE("areas/{id}")
    suspend fun excluir(@Path("id") id: String): Response<ApiEnvelope<Unit>>
}
