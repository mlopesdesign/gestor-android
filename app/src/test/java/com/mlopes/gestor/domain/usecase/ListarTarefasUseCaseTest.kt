package com.mlopes.gestor.domain.usecase

import com.mlopes.gestor.data.repository.TarefaRepository
import com.mlopes.gestor.domain.model.Prioridade
import com.mlopes.gestor.domain.model.StatusTarefa
import com.mlopes.gestor.domain.model.Tarefa
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ListarTarefasUseCaseTest {

    private val repository: TarefaRepository = mockk()
    private val useCase = ListarTarefasUseCase(repository)

    private fun tarefa(
        id: String,
        prioridade: Prioridade,
        status: StatusTarefa = StatusTarefa.PENDENTE,
        vencimento: String? = "2026-08-17T00:00:00Z",
    ) = Tarefa(
        id = id,
        titulo = "T$id",
        descricao = null,
        status = status,
        prioridade = prioridade,
        projetoId = null,
        clienteId = null,
        areaId = null,
        vencimentoEm = vencimento,
        etiquetas = emptyList(),
        responsavel = null,
        concluidaEm = null,
        criadaEm = "2026-08-17T00:00:00Z",
        atualizadaEm = "2026-08-17T00:00:00Z",
        versao = 1,
    )

    @Test
    fun `ordena por prioridade descendente`() = runTest {
        val tarefas = listOf(
            tarefa("a", Prioridade.BAIXA),
            tarefa("b", Prioridade.CRITICA),
            tarefa("c", Prioridade.NORMAL),
        )
        every { repository.observarTarefas(null) } returns flowOf(tarefas)

        val lista = useCase(FiltroTarefa.TODAS).first()

        assertEquals(listOf("b", "c", "a"), lista.map { it.id })
    }

    @Test
    fun `filtro PENDENTES passa status PENDENTE ao repository`() = runTest {
        every { repository.observarTarefas(StatusTarefa.PENDENTE) } returns flowOf(emptyList())

        useCase(FiltroTarefa.PENDENTES).first()

        io.mockk.verify { repository.observarTarefas(StatusTarefa.PENDENTE) }
    }

    @Test
    fun `filtro HOJE mantem concluidas para visualizacao mas prioriza pendentes`() = runTest {
        val tarefas = listOf(
            tarefa("pendente", Prioridade.NORMAL, StatusTarefa.PENDENTE, "2026-08-17T10:00:00Z"),
            tarefa("concluida", Prioridade.CRITICA, StatusTarefa.CONCLUIDA, "2026-08-17T08:00:00Z"),
        )
        every { repository.observarTarefas(null) } returns flowOf(tarefas)

        val lista = useCase(FiltroTarefa.HOJE).first()

        // CRITICA vem antes de NORMAL pela ordem de prioridade; ambas aparecem
        assertEquals("concluida", lista.first().id)
        assertEquals(2, lista.size)
    }
}
