package com.mlopes.gestor.data.remote.dto

import com.mlopes.gestor.data.local.entity.AreaEntity
import com.mlopes.gestor.domain.model.Area
import kotlinx.serialization.Serializable

@Serializable
data class AreaDto(
    val id: String,
    val nome: String,
    val cor: String? = null,
    val icone: String? = null,
    val ordem: Int = 0,
    val criadoEm: String,
    val atualizadoEm: String,
)

@Serializable
data class AreaInputDto(
    val nome: String,
    val cor: String? = null,
    val icone: String? = null,
    val ordem: Int = 0,
)

fun AreaDto.toEntity(): AreaEntity = AreaEntity(
    id = id,
    nome = nome,
    cor = cor,
    icone = icone,
    ordem = ordem,
    criadoEm = criadoEm,
)

fun AreaEntity.toDomain(): Area = Area(
    id = id,
    nome = nome,
    cor = cor,
    icone = icone,
    ordem = ordem,
    criadoEm = criadoEm,
)

fun AreaDto.toDomain(): Area = Area(
    id = id,
    nome = nome,
    cor = cor,
    icone = icone,
    ordem = ordem,
    criadoEm = criadoEm,
)
