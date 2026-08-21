package com.mlopes.gestor.data.remote.dto

import com.mlopes.gestor.data.local.entity.TarefaEntity
import com.mlopes.gestor.domain.model.Prioridade
import com.mlopes.gestor.domain.model.StatusTarefa
import com.mlopes.gestor.domain.model.Tarefa
import kotlinx.serialization.Serializable

@Serializable
data class TarefaDto(
    val id: String,
    val titulo: String,
    val descricao: String? = null,
    val status: String,
    val prioridade: String,
    val nivelCobranca: String = "PERSISTENTE",
    val areaId: String? = null,
    val projetoId: String? = null,
    val clienteId: String? = null,
    val inicioEm: String? = null,
    val vencimentoEm: String? = null,
    val duracaoEstimadaMin: Int? = null,
    val duracaoRealizadaMin: Int = 0,
    val etiquetas: List<String> = emptyList(),
    val responsavel: String? = null,
    val origem: String = "MANUAL",
    val concluidaEm: String? = null,
    val entregueEm: String? = null,
    val criadoEm: String,
    val atualizadoEm: String,
    val versao: Int,
    val deletadoEm: String? = null,
)

@Serializable
data class TarefaInputDto(
    val titulo: String,
    val descricao: String? = null,
    val status: String? = null,
    val prioridade: String? = null,
    val projetoId: String? = null,
    val clienteId: String? = null,
    val areaId: String? = null,
    val vencimentoEm: String? = null,
    val etiquetas: List<String> = emptyList(),
    val versaoBase: Int? = null,
)

@Serializable
data class TarefaConcluidaRequest(
    val confirmada: Boolean = true,
)

fun TarefaDto.toEntity(): TarefaEntity = TarefaEntity(
    id = id,
    titulo = titulo,
    descricao = descricao,
    status = status,
    prioridade = prioridade,
    projetoId = projetoId,
    clienteId = clienteId,
    areaId = areaId,
    vencimentoEm = vencimentoEm,
    etiquetas = etiquetas.joinToString(","),
    responsavel = responsavel,
    concluidaEm = concluidaEm,
    criadoEm = criadoEm,
    atualizadoEm = atualizadoEm,
    versao = versao,
    pendenteSync = false,
)

fun TarefaEntity.toDomain(): Tarefa = Tarefa(
    id = id,
    titulo = titulo,
    descricao = descricao,
    status = StatusTarefa.fromApi(status),
    prioridade = Prioridade.fromApi(prioridade),
    projetoId = projetoId,
    clienteId = clienteId,
    areaId = areaId,
    vencimentoEm = vencimentoEm,
    etiquetas = if (etiquetas.isBlank()) emptyList() else etiquetas.split(","),
    responsavel = responsavel,
    concluidaEm = concluidaEm,
    criadoEm = criadoEm,
    atualizadoEm = atualizadoEm,
    versao = versao,
    pendenteSync = pendenteSync,
)

fun TarefaDto.toDomain(): Tarefa = Tarefa(
    id = id,
    titulo = titulo,
    descricao = descricao,
    status = StatusTarefa.fromApi(status),
    prioridade = Prioridade.fromApi(prioridade),
    projetoId = projetoId,
    clienteId = clienteId,
    areaId = areaId,
    vencimentoEm = vencimentoEm,
    etiquetas = etiquetas,
    responsavel = responsavel,
    concluidaEm = concluidaEm,
    criadoEm = criadoEm,
    atualizadoEm = atualizadoEm,
    versao = versao,
)
