package com.mlopes.gestor.data.remote.api

import com.mlopes.gestor.data.remote.dto.ApiEnvelope
import com.mlopes.gestor.data.remote.dto.LoginData
import com.mlopes.gestor.data.remote.dto.LoginRequest
import com.mlopes.gestor.data.remote.dto.LogoutResponse
import com.mlopes.gestor.data.remote.dto.RefreshData
import com.mlopes.gestor.data.remote.dto.UsuarioDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Response<ApiEnvelope<LoginData>>

    @POST("auth/refresh")
    suspend fun refresh(): Response<ApiEnvelope<RefreshData>>

    @POST("auth/logout")
    suspend fun logout(): Response<ApiEnvelope<LogoutResponse>>

    @GET("auth/me")
    suspend fun me(): Response<ApiEnvelope<UsuarioDto>>
}
