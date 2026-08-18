package com.mlopes.gestor.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "areas")
data class AreaEntity(
    @PrimaryKey val id: String,
    val nome: String,
    val cor: String?,
    val icone: String?,
    val ordem: Int,
    val criadoEm: String,
)
