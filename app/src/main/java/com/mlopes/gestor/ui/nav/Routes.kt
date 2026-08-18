package com.mlopes.gestor.ui.nav

/**
 * Rotas do NavHost. Padrao string com parametros nomeados.
 */
sealed class Route(val path: String) {
    data object Login : Route("login")
    data object Home : Route("home")
    data object Tarefas : Route("tarefas")
    data object TarefaDetalhe : Route("tarefa/{id}") {
        fun criar(id: String) = "tarefa/$id"
    }
    data object TarefaEditar : Route("tarefa/editar?id={id}") {
        fun criar(id: String? = null) = "tarefa/editar?id=${id ?: ""}"
    }
    data object Projetos : Route("projetos")
    data object Clientes : Route("clientes")
    data object Areas : Route("areas")
    data object Config : Route("config")
}
