package com.mlopes.gestor.domain.model

data class Cliente(
    val id: String,
    val nome: String,
    val email: String?,
    val telefone: String?,
    val observacoes: String?,
    val ativo: Boolean,
    val criadoEm: String,
    val atualizadoEm: String,
)
