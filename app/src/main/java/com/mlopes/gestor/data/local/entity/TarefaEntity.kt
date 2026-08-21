package com.mlopes.gestor.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tarefas",
    indices = [Index("status"), Index("projetoId"), Index("clienteId"), Index("vencimentoEm")],
)
data class TarefaEntity(
    @PrimaryKey val id: String,
    val titulo: String,
    val descricao: String?,
    val status: String,
    val prioridade: String,
    val projetoId: String?,
    val clienteId: String?,
    val areaId: String?,
    val vencimentoEm: String?,
    /** Etiquetas separadas por virgula. */
    val etiquetas: String,
    val responsavel: String?,
    val concluidaEm: String?,
    val criadoEm: String,
    val atualizadoEm: String,
    val versao: Int,
    val pendenteSync: Boolean = false,
)
