package com.mlopes.gestor.data.repository

import com.mlopes.gestor.data.remote.TokenStorage
import com.mlopes.gestor.data.remote.api.AuthApi
import com.mlopes.gestor.data.remote.dto.ApiEnvelope
import com.mlopes.gestor.data.remote.dto.LoginData
import com.mlopes.gestor.data.remote.dto.LoginRequest
import com.mlopes.gestor.data.remote.dto.UsuarioDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.time.Instant

class AuthRepositoryTest {

    private val api: AuthApi = mockk()
    private val tokenStorage: TokenStorage = mockk(relaxed = true)

    private val repo = AuthRepository(api, tokenStorage)

    @Test
    fun `login bem-sucedido salva token e expira em`() = runTest {
        val expira = Instant.now().plusSeconds(3600)
        coEvery { api.login(any()) } returns Response.success(
            ApiEnvelope(success = true, data = LoginData(
                token = "abc",
                expiraEm = expira.toString(),
                usuario = UsuarioDto(id = "u1", email = "a@b.c", nome = "Teste"),
            ))
        )

        val result = repo.login("a@b.c", "123")

        assertTrue(result.isSuccess)
        verify { tokenStorage.salvar("abc", expira) }
    }

    @Test
    fun `login com 401 retorna excecao de credenciais`() = runTest {
        coEvery { api.login(any()) } returns Response.error(
            401, "".toResponseBody("application/json".toMediaTypeOrNull())
        )

        val result = repo.login("a@b.c", "errada")

        assertTrue(result.isFailure)
        assertEquals("E-mail ou senha incorretos.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `logout limpa token e chama api`() = runTest {
        coEvery { api.logout() } returns Response.success(ApiEnvelope(success = true))

        repo.logout()

        coVerify { api.logout() }
        verify { tokenStorage.limpar() }
    }

    @Test
    fun `logado retorna true quando token existe e nao expirou`() {
        every { tokenStorage.buscar() } returns "token-ativo"
        every { tokenStorage.expirou() } returns false

        assertTrue(repo.logado())
    }

    @Test
    fun `logado retorna false quando tokenStorage sem token`() {
        every { tokenStorage.buscar() } returns null

        assertFalse(repo.logado())
    }

    @Test
    fun `logado retorna false quando token expirou`() {
        every { tokenStorage.buscar() } returns "token"
        every { tokenStorage.expirou() } returns true

        assertFalse(repo.logado())
    }
}
