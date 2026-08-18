package com.mlopes.gestor.domain.model

/**
 * Prioridade da tarefa. Alinhada com a coluna `prioridade` do MySQL.
 */
enum class Prioridade {
    BAIXA,
    NORMAL,
    ALTA,
    CRITICA;

    companion object {
        fun fromApi(value: String?): Prioridade = when (value?.uppercase()) {
            "ALTA" -> ALTA
            "CRITICA" -> CRITICA
            "BAIXA" -> BAIXA
            else -> NORMAL
        }
    }

    /** Peso para ordenacao (maior = mais urgente). */
    val peso: Int
        get() = when (this) {
            CRITICA -> 4
            ALTA -> 3
            NORMAL -> 2
            BAIXA -> 1
        }
}
