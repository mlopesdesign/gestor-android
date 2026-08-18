package com.mlopes.gestor.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projetos")
data class ProjetoEntity(
    @PrimaryKey val id: String,
    val nome: String,
    val descricao: String?,
    val status: String,
    val clienteId: String?,
    val areaId: String?,
    val criadoEm: String,
    val atualizadoEm: String,
    val versao: Int,
)
