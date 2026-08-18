package com.mlopes.gestor.domain.model

data class Projeto(
    val id: String,
    val nome: String,
    val descricao: String?,
    val status: String,
    val clienteId: String?,
    val areaId: String?,
    val criadoEm: String,
    val atualizadoEm: String,
    val versao: Int,
)
