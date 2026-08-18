package com.mlopes.gestor.ui

import androidx.lifecycle.ViewModel
import com.mlopes.gestor.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel raiz. Expõe o AuthRepository e a decisão de destino inicial
 * (Login ou Home) para o NavHost. Usado uma unica vez no MainActivity.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    val authRepository: AuthRepository,
) : ViewModel() {
    val logado: Boolean = authRepository.logado()
}
