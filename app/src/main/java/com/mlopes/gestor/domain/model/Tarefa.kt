package com.mlopes.gestor.domain.model

/**
 * Modelo de dominio da Tarefa. Puro, sem dependencia de framework.
 */
data class Tarefa(
    val id: String,
    val titulo: String,
    val descricao: String?,
    val status: StatusTarefa,
    val prioridade: Prioridade,
    val projetoId: String?,
    val clienteId: String?,
    val areaId: String?,
    val vencimentoEm: String?,
    val etiquetas: List<String>,
    val responsavel: String?,
    val concluidaEm: String?,
    val criadaEm: String,
    val atualizadaEm: String,
    val versao: Int,
    val pendenteSync: Boolean = false,
)
