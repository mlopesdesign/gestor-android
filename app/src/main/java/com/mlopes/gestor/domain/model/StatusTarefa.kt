package com.mlopes.gestor.domain.model

/**
 * Status possiveis de uma tarefa. Espelha a coluna `status` do MySQL do plugin WP.
 */
enum class StatusTarefa {
    PENDENTE,
    EM_ANDAMENTO,
    CONCLUIDA,
    CANCELADA,
    ARQUIVADA;

    companion object {
        fun fromApi(value: String?): StatusTarefa = when (value?.uppercase()) {
            "EM_ANDAMENTO" -> EM_ANDAMENTO
            "CONCLUIDA" -> CONCLUIDA
            "CANCELADA" -> CANCELADA
            "ARQUIVADA" -> ARQUIVADA
            else -> PENDENTE
        }
    }
}
