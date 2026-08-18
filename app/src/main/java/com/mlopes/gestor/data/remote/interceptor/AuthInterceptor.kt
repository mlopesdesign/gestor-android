package com.mlopes.gestor.data.remote.interceptor

import com.mlopes.gestor.data.remote.TokenStorage
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Injeta o header `Authorization: Bearer <token>` em toda requisicao,
 * exceto quando o caminho e /auth/login (evita mandar token invalido).
 */
class AuthInterceptor @Inject constructor(
    private val tokenStorage: TokenStorage,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val isLogin = original.url.encodedPath.contains("/auth/login")
        val builder = original.newBuilder()
            .header("Accept", "application/json")
            .header("User-Agent", "GestorAndroid/0.1.0")
        if (!isLogin) {
            tokenStorage.buscar()?.let { token ->
                builder.header("Authorization", "Bearer $token")
            }
        }
        return chain.proceed(builder.build())
    }
}
