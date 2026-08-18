package com.mlopes.gestor.data.remote.dto

import com.mlopes.gestor.data.local.entity.ProjetoEntity
import com.mlopes.gestor.domain.model.Projeto
import kotlinx.serialization.Serializable

@Serializable
data class ProjetoDto(
    val id: String,
    val nome: String,
    val descricao: String? = null,
    val status: String,
    val clienteId: String? = null,
    val areaId: String? = null,
    val inicioEm: String? = null,
    val fimEm: String? = null,
    val progresso: Int = 0,
    val criadoEm: String,
    val atualizadoEm: String,
    val versao: Int,
)

@Serializable
data class ProjetoInputDto(
    val nome: String,
    val descricao: String? = null,
    val status: String? = null,
    val clienteId: String? = null,
    val areaId: String? = null,
    val versaoBase: Int? = null,
)

fun ProjetoDto.toEntity(): ProjetoEntity = ProjetoEntity(
    id = id,
    nome = nome,
    descricao = descricao,
    status = status,
    clienteId = clienteId,
    areaId = areaId,
    criadoEm = criadoEm,
    atualizadoEm = atualizadoEm,
    versao = versao,
)

fun ProjetoEntity.toDomain(): Projeto = Projeto(
    id = id,
    nome = nome,
    descricao = descricao,
    status = status,
    clienteId = clienteId,
    areaId = areaId,
    criadoEm = criadoEm,
    atualizadoEm = atualizadoEm,
    versao = versao,
)

fun ProjetoDto.toDomain(): Projeto = Projeto(
    id = id,
    nome = nome,
    descricao = descricao,
    status = status,
    clienteId = clienteId,
    areaId = areaId,
    criadoEm = criadoEm,
    atualizadoEm = atualizadoEm,
    versao = versao,
)
