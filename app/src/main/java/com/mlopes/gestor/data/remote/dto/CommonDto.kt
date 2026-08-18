package com.mlopes.gestor.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Envelope padrao do plugin WP, conforme docs/GUIA-API.md §1.1.
 * Toda resposta do servidor vem em { success, data }.
 */
@Serializable
data class ApiEnvelope<T>(
    val success: Boolean,
    val data: T? = null,
    val code: String? = null,
    val message: String? = null,
    @SerialName("data_error") val dataError: ApiErrorData? = null,
)

@Serializable
data class ApiErrorData(
    val status: Int? = null,
)

/**
 * Resposta de listagem com cursor (sync). Vide GUIA-API §4.1.
 */
@Serializable
data class CursorEnvelope<T>(
    val items: List<T> = emptyList(),
    val total: Int = 0,
    val nextCursor: String? = null,
    val hasMore: Boolean = false,
    val serverTime: String? = null,
)
