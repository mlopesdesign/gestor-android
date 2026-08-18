package com.mlopes.gestor.data.repository

import com.mlopes.gestor.data.local.dao.PendingOpDao
import com.mlopes.gestor.data.local.dao.TarefaDao
import com.mlopes.gestor.data.local.entity.PendingOpEntity
import com.mlopes.gestor.data.local.entity.TarefaEntity
import com.mlopes.gestor.data.remote.api.TarefasApi
import com.mlopes.gestor.domain.model.Prioridade
import com.mlopes.gestor.domain.model.StatusTarefa
import com.mlopes.gestor.domain.usecase.TarefaInput
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TarefaRepositoryTest {

    private val api: TarefasApi = mockk()
    private val dao: TarefaDao = mockk(relaxed = true)
    private val pendingOpDao: PendingOpDao = mockk(relaxed = true)
    private val networkMonitor: NetworkMonitor = mockk()

    private val repo = TarefaRepository(api, dao, pendingOpDao, networkMonitor)

    private val tarefaLocal = TarefaEntity(
        id = "t1",
        titulo = "Teste",
        descricao = null,
        status = StatusTarefa.PENDENTE.name,
        prioridade = Prioridade.NORMAL.name,
        projetoId = null,
        clienteId = null,
        areaId = null,
        vencimentoEm = null,
        etiquetas = "",
        responsavel = null,
        concluidaEm = null,
        criadaEm = "2026-08-17T00:00:00Z",
        atualizadaEm = "2026-08-17T00:00:00Z",
        versao = 1,
    )

    @Test
    fun `observarTarefas emite lista do Room`() {
        every { dao.observarTodas(true) } returns flowOf(listOf(tarefaLocal))

        val result = repo.observarTarefas()

        assertNotNull(result)
    }

    @Test
    fun `criar offline insere local com pendenteSync e enfileira op`() = runTest {
        every { networkMonitor.connected() } returns false

        val input = TarefaInput(titulo = "Nova")
        val opSlot = slot<PendingOpEntity>()

        val result = repo.criar(input)

        assertTrue(result.isSuccess)
        coVerify { dao.inserir(match { it.titulo == "Nova" && it.pendenteSync }) }
        coVerify { pendingOpDao.enfileirar(capture(opSlot)) }
        assertEquals("tarefas", opSlot.captured.tabela)
        assertEquals("UPSERT", opSlot.captured.operacao)
    }

    @Test
    fun `concluir atualiza status local e enfileira`() = runTest {
        coEvery { dao.buscarPorId("t1") } returns tarefaLocal

        val result = repo.concluir("t1")

        assertTrue(result.isSuccess)
        coVerify {
            dao.inserir(match {
                it.status == StatusTarefa.CONCLUIDA.name && it.pendenteSync
            })
        }
        coVerify { pendingOpDao.enfileirar(any()) }
    }

    @Test
    fun `excluir remove do Room e enfileira delete`() = runTest {
        val opSlot = slot<PendingOpEntity>()

        val result = repo.excluir("t1")

        assertTrue(result.isSuccess)
        coVerify { dao.deletar("t1") }
        coVerify { pendingOpDao.enfileirar(capture(opSlot)) }
        assertEquals("DELETE", opSlot.captured.operacao)
        assertEquals("t1", opSlot.captured.registroId)
    }
}
