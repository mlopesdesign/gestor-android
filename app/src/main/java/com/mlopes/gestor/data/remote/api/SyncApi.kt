package com.mlopes.gestor.data.remote.api

import com.mlopes.gestor.data.remote.dto.ApiEnvelope
import com.mlopes.gestor.data.remote.dto.ConflitoListResponse
import com.mlopes.gestor.data.remote.dto.PullRequest
import com.mlopes.gestor.data.remote.dto.PullResponse
import com.mlopes.gestor.data.remote.dto.PushRequest
import com.mlopes.gestor.data.remote.dto.PushResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SyncApi {
    @GET("sync/pull")
    suspend fun pull(
        @Query("dispositivo_id") dispositivoId: String,
        @Query("since") since: String? = null,
        @Query("limit") limit: Int = 200,
        @Query("offset") offset: Int = 0,
    ): Response<ApiEnvelope<PullResponse>>

    @POST("sync/push")
    suspend fun push(@Body body: PushRequest): Response<ApiEnvelope<PushResponse>>

    @GET("sync/conflitos")
    suspend fun listarConflitos(): Response<ApiEnvelope<ConflitoListResponse>>
}
