package com.mlopes.gestor.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Operacao pendente na fila offline. Quando a rede volta, SyncRepository
 * faz flush destes registros contra o servidor.
 */
@Entity(tableName = "pending_ops")
data class PendingOpEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tabela: String,
    val operacao: String,
    val registroId: String,
    val payloadJson: String,
    val criadoEm: String,
    val tentativas: Int = 0,
    val ultimoErro: String? = null,
)
