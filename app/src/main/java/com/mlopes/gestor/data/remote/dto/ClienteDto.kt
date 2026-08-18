package com.mlopes.gestor.data.remote.dto

import com.mlopes.gestor.data.local.entity.ClienteEntity
import com.mlopes.gestor.domain.model.Cliente
import kotlinx.serialization.Serializable

@Serializable
data class ClienteDto(
    val id: String,
    val nome: String,
    val email: String? = null,
    val telefone: String? = null,
    val documento: String? = null,
    val observacoes: String? = null,
    val ativo: Boolean = true,
    val criadoEm: String,
    val atualizadoEm: String,
    val versao: Int = 1,
)

@Serializable
data class ClienteInputDto(
    val nome: String,
    val email: String? = null,
    val telefone: String? = null,
    val documento: String? = null,
    val observacoes: String? = null,
    val ativo: Boolean = true,
    val versaoBase: Int? = null,
)

fun ClienteDto.toEntity(): ClienteEntity = ClienteEntity(
    id = id,
    nome = nome,
    email = email,
    telefone = telefone,
    observacoes = observacoes,
    ativo = ativo,
    criadoEm = criadoEm,
    atualizadoEm = atualizadoEm,
)

fun ClienteEntity.toDomain(): Cliente = Cliente(
    id = id,
    nome = nome,
    email = email,
    telefone = telefone,
    observacoes = observacoes,
    ativo = ativo,
    criadoEm = criadoEm,
    atualizadoEm = atualizadoEm,
)

fun ClienteDto.toDomain(): Cliente = Cliente(
    id = id,
    nome = nome,
    email = email,
    telefone = telefone,
    observacoes = observacoes,
    ativo = ativo,
    criadoEm = criadoEm,
    atualizadoEm = atualizadoEm,
)
