package com.mlopes.gestor.data.remote.api

import com.mlopes.gestor.data.remote.dto.ApiEnvelope
import com.mlopes.gestor.data.remote.dto.CursorEnvelope
import com.mlopes.gestor.data.remote.dto.ProjetoDto
import com.mlopes.gestor.data.remote.dto.ProjetoInputDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ProjetosApi {
    @GET("projetos")
    suspend fun listar(
        @Query("since") since: String? = null,
        @Query("limit") limit: Int = 200,
    ): Response<ApiEnvelope<CursorEnvelope<ProjetoDto>>>

    @GET("projetos/{id}")
    suspend fun buscar(@Path("id") id: String): Response<ApiEnvelope<ProjetoDto>>

    @POST("projetos")
    suspend fun criar(@Body body: ProjetoInputDto): Response<ApiEnvelope<ProjetoDto>>

    @PUT("projetos/{id}")
    suspend fun atualizar(
        @Path("id") id: String,
        @Body body: ProjetoInputDto,
    ): Response<ApiEnvelope<ProjetoDto>>

    @DELETE("projetos/{id}")
    suspend fun excluir(@Path("id") id: String): Response<ApiEnvelope<Unit>>
}
