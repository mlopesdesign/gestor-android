package com.mlopes.gestor

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application root. Hilt gera o grafo de injecao de dependencia aqui.
 * Nenhuma logica de negocio neste arquivo alem do bootstrap.
 */
@HiltAndroidApp
class GestorApp : Application()
