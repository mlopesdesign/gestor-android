package com.mlopes.gestor.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clientes")
data class ClienteEntity(
    @PrimaryKey val id: String,
    val nome: String,
    val email: String?,
    val telefone: String?,
    val observacoes: String?,
    val ativo: Boolean,
    val criadoEm: String,
    val atualizadoEm: String,
)
