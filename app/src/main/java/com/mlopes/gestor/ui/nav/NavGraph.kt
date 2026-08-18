package com.mlopes.gestor.ui.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.mlopes.gestor.R
import com.mlopes.gestor.data.repository.AuthRepository
import com.mlopes.gestor.ui.areas.AreasScreen
import com.mlopes.gestor.ui.auth.LoginScreen
import com.mlopes.gestor.ui.clientes.ClientesScreen
import com.mlopes.gestor.ui.config.ConfigScreen
import com.mlopes.gestor.ui.projetos.ProjetosScreen
import com.mlopes.gestor.ui.tarefas.TarefaDetalheScreen
import com.mlopes.gestor.ui.tarefas.TarefaEditarScreen
import com.mlopes.gestor.ui.tarefas.TarefasScreen

@Composable
fun GestorNavHost(
    authRepository: AuthRepository,
    navController: NavHostController = rememberNavController(),
) {
    // `authRepository` ja vem injetado via MainViewModel; usamos a variavel
    // local para clareza e para o destruturador do start destination.
    val logado = authRepository.logado()
    val startDestination = if (logado) Route.Home.path else Route.Login.path
    val currentEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute != null && currentRoute != Route.Login.path && !currentRoute.startsWith("tarefa/")) {
                BottomBar(navController, currentRoute)
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Route.Login.path) {
                LoginScreen(
                    onLogado = {
                        navController.navigate(Route.Home.path) {
                            popUpTo(Route.Login.path) { inclusive = true }
                        }
                    },
                )
            }
            composable(Route.Home.path) {
                TarefasScreen(
                    navController = navController,
                    inicial = com.mlopes.gestor.domain.usecase.FiltroTarefa.HOJE,
                )
            }
            composable(Route.Tarefas.path) {
                TarefasScreen(
                    navController = navController,
                    inicial = com.mlopes.gestor.domain.usecase.FiltroTarefa.TODAS,
                )
            }
            composable(
                route = Route.TarefaDetalhe.path,
                arguments = listOf(navArgument("id") { type = NavType.StringType }),
            ) { backStack ->
                val id = backStack.arguments?.getString("id").orEmpty()
                TarefaDetalheScreen(
                    tarefaId = id,
                    onVoltar = { navController.popBackStack() },
                    onEditar = { navController.navigate(Route.TarefaEditar.criar(id)) },
                )
            }
            composable(
                route = Route.TarefaEditar.path,
                arguments = listOf(navArgument("id") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }),
            ) { backStack ->
                val id = backStack.arguments?.getString("id")?.takeIf { it.isNotBlank() }
                TarefaEditarScreen(
                    tarefaId = id,
                    onVoltar = { navController.popBackStack() },
                )
            }
            composable(Route.Projetos.path) { ProjetosScreen() }
            composable(Route.Clientes.path) { ClientesScreen() }
            composable(Route.Areas.path) { AreasScreen() }
            composable(Route.Config.path) {
                ConfigScreen(
                    onSair = {
                        navController.navigate(Route.Login.path) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun BottomBar(navController: NavHostController, currentRoute: String) {
    NavigationBar {
        BottomItem(navController, currentRoute, Route.Home, R.string.nav_tarefas, Icons.Filled.Home)
        BottomItem(navController, currentRoute, Route.Projetos, R.string.nav_projetos, Icons.Filled.Build)
        BottomItem(navController, currentRoute, Route.Clientes, R.string.nav_clientes, Icons.Filled.Person)
        BottomItem(navController, currentRoute, Route.Areas, R.string.nav_areas, Icons.Filled.Check)
        BottomItem(navController, currentRoute, Route.Config, R.string.nav_config, Icons.Filled.Settings)
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.BottomItem(
    navController: NavHostController,
    currentRoute: String?,
    route: Route,
    labelRes: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    NavigationBarItem(
        selected = currentRoute == route.path,
        onClick = {
            navController.navigate(route.path) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        },
        icon = { Icon(icon, contentDescription = null) },
        label = { Text(stringResource(labelRes)) },
    )
}
