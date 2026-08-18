package com.mlopes.gestor.domain.model

data class Area(
    val id: String,
    val nome: String,
    val cor: String?,
    val icone: String?,
    val ordem: Int,
    val criadoEm: String,
)
