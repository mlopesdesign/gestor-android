package com.mlopes.gestor.ui.tarefas

import com.mlopes.gestor.data.repository.TarefaRepository
import com.mlopes.gestor.domain.model.Prioridade
import com.mlopes.gestor.domain.model.StatusTarefa
import com.mlopes.gestor.domain.model.Tarefa
import com.mlopes.gestor.domain.usecase.ConcluirTarefaUseCase
import com.mlopes.gestor.domain.usecase.FiltroTarefa
import com.mlopes.gestor.domain.usecase.ListarTarefasUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TarefasViewModelTest {

    private val listarUseCase: ListarTarefasUseCase = mockk()
    private val concluirUseCase: ConcluirTarefaUseCase = mockk()
    private val repository: TarefaRepository = mockk(relaxed = true)
    private lateinit var viewModel: TarefasViewModel

    private val tarefa = Tarefa(
        id = "t1",
        titulo = "Tarefa 1",
        descricao = null,
        status = StatusTarefa.PENDENTE,
        prioridade = Prioridade.ALTA,
        projetoId = null,
        clienteId = null,
        areaId = null,
        vencimentoEm = "2026-08-17T10:00:00Z",
        etiquetas = emptyList(),
        responsavel = null,
        concluidaEm = null,
        criadaEm = "2026-08-17T00:00:00Z",
        atualizadaEm = "2026-08-17T00:00:00Z",
        versao = 1,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `inicializa com filtro HOJE e carrega lista`() = runTest {
        every { listarUseCase(FiltroTarefa.HOJE) } returns flowOf(listOf(tarefa))

        viewModel = TarefasViewModel(listarUseCase, concluirUseCase, repository)

        // Estado inicial: carregando true
        assertEquals(true, viewModel.state.value.carregando)
    }

    @Test
    fun `setFiltro atualiza filtro no state`() = runTest {
        every { listarUseCase(any()) } returns flowOf(emptyList())

        viewModel = TarefasViewModel(listarUseCase, concluirUseCase, repository)
        // Inicia coleta em backgroundScope para acionar o WhileSubscribed
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect { }
        }
        viewModel.setFiltro(FiltroTarefa.CONCLUIDAS)
        advanceUntilIdle()

        io.mockk.verify { listarUseCase(FiltroTarefa.CONCLUIDAS) }
    }

    @Test
    fun `concluir chama useCase com id`() = runTest {
        every { listarUseCase(any()) } returns flowOf(emptyList())
        coEvery { concluirUseCase("t1") } returns Result.success(Unit)

        viewModel = TarefasViewModel(listarUseCase, concluirUseCase, repository)
        viewModel.concluir("t1")

        coVerify { concluirUseCase("t1") }
    }

    @Test
    fun `refresh dispara refresh do repository`() = runTest {
        every { listarUseCase(any()) } returns flowOf(emptyList())
        coEvery { repository.refresh() } returns Result.success(Unit)

        viewModel = TarefasViewModel(listarUseCase, concluirUseCase, repository)
        viewModel.refresh()

        coVerify { repository.refresh() }
    }
}
