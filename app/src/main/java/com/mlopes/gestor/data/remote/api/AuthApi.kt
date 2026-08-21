package com.mlopes.gestor.data.remote.api

import com.mlopes.gestor.data.remote.dto.ApiEnvelope
import com.mlopes.gestor.data.remote.dto.LoginData
import com.mlopes.gestor.data.remote.dto.LoginRequest
import com.mlopes.gestor.data.remote.dto.UsuarioDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Auth + FCM. Endpoints do plugin WP da API.
 */
interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Response<ApiEnvelope<LoginData>>

    @POST("auth/logout")
    suspend fun logout(): Response<Unit>

    @GET("auth/me")
    suspend fun me(): Response<ApiEnvelope<UsuarioDto>>

    /** FIX v0.1.5 (F6 Notificacoes): registrar token FCM do dispositivo. */
    @POST("sync/fcm-token")
    suspend fun enviarFcmToken(@Body body: FcmTokenRequest): Response<Unit>
}

@kotlinx.serialization.Serializable
data class FcmTokenRequest(
    val token: String,
    val plataforma: String = "ANDROID",
)
