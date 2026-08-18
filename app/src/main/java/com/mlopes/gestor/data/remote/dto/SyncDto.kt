package com.mlopes.gestor.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class MudancaDto(
    val tabela: String,
    val operacao: String,
    val registroId: String,
    val payload: kotlinx.serialization.json.JsonElement,
    val versao: Int = 1,
    val atualizadoEm: String,
)

@Serializable
data class PullResponse(
    val mudancas: List<MudancaDto> = emptyList(),
    val nextCursor: String? = null,
    val hasMore: Boolean = false,
    val serverTime: String? = null,
)

@Serializable
data class PullRequest(
    val dispositivoId: String,
    val since: String? = null,
    val limit: Int = 200,
    val offset: Int = 0,
)

@Serializable
data class PushRequest(
    val dispositivoId: String,
    val mutacoes: List<MutacaoDto>,
)

@Serializable
data class MutacaoDto(
    val tabela: String,
    val operacao: String,
    val registroId: String,
    val versaoBase: Int? = null,
    val payload: kotlinx.serialization.json.JsonElement,
)

@Serializable
data class PushResponse(
    val aplicadas: Int = 0,
    val conflitos: List<ConflitoDto> = emptyList(),
    val serverTime: String? = null,
)

@Serializable
data class ConflitoDto(
    val tabela: String,
    val registroId: String,
    val versaoServidor: Int,
    val versaoCliente: Int,
    val payloadServidor: kotlinx.serialization.json.JsonElement,
    val estado: String = "PENDENTE",
)

@Serializable
data class ConflitoListResponse(
    val items: List<ConflitoDto> = emptyList(),
)
