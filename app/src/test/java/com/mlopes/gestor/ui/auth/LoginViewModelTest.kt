package com.mlopes.gestor.ui.auth

import com.mlopes.gestor.domain.usecase.LoginUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val loginUseCase: LoginUseCase = mockk()
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = LoginViewModel(loginUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `estado inicial tem campos vazios e sem erro`() {
        val state = viewModel.state.value
        assertEquals("", state.email)
        assertEquals("", state.senha)
        assertFalse(state.carregando)
        assertNull(state.erro)
    }

    @Test
    fun `entrar com campos vazios mostra erro inline sem chamar useCase`() = runTest {
        viewModel.entrar()

        val state = viewModel.state.value
        assertNotNull(state.erro)
        assertFalse(state.carregando)
        io.mockk.coVerify(exactly = 0) { loginUseCase(any(), any()) }
    }

    @Test
    fun `entrar com sucesso vai para estado logado`() = runTest {
        coEvery { loginUseCase("a@b.c", "senha") } returns Result.success(Unit)

        viewModel.onEmailChange("a@b.c")
        viewModel.onSenhaChange("senha")
        viewModel.entrar()

        val state = viewModel.state.value
        assertTrue(state.logado)
        assertFalse(state.carregando)
        assertNull(state.erro)
    }

    @Test
    fun `entrar com falha mostra erro e nao loga`() = runTest {
        coEvery { loginUseCase("a@b.c", "errada") } returns Result.failure(SecurityException("E-mail ou senha incorretos."))

        viewModel.onEmailChange("a@b.c")
        viewModel.onSenhaChange("errada")
        viewModel.entrar()

        val state = viewModel.state.value
        assertFalse(state.logado)
        assertEquals("E-mail ou senha incorretos.", state.erro)
        assertFalse(state.carregando)
    }
}
