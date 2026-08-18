package com.mlopes.gestor.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val senha: String,
    val dispositivoId: String,
    val sistema: String = "ANDROID",
    val appVersao: String = "0.1.0",
)

@Serializable
data class LoginData(
    val token: String,
    val expiraEm: String,
    val usuario: UsuarioDto,
)

@Serializable
data class UsuarioDto(
    val id: String,
    val email: String,
    val nome: String,
    val fuso: String? = null,
    val papel: String? = null,
)

@Serializable
data class RefreshData(
    val token: String,
    val expiraEm: String,
)

@Serializable
data class LogoutResponse(
    val success: Boolean = true,
)
