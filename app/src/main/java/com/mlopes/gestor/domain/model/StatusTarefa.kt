package com.mlopes.gestor.domain.model

/**
 * Status possiveis de uma tarefa. Espelha os valores aceitos pelo plugin WP
 * (`gestor/v1/tarefas`) — vide schema MySQL `wp_gestor_tarefas.status`.
 *
 * Mantemos nomes amigaveis para o domain; aliases cobrem a divergencia
 * historica (Android antigo usava "PENDENTE", WP atual usa "PLANEJADA",
 * tarefa que chegou da inbox aparece como "CAIXA_ENTRADA", etc).
 */
enum class StatusTarefa {
    CAIXA_ENTRADA,
    PLANEJADA,            // WP: tarefa agendada mas nao iniciada
    EM_ANDAMENTO,
    AGUARDANDO_TERCEIRO,
    BLOQUEADA,
    EM_REVISAO,
    ENTREGUE_AGUARDANDO_CONFIRMACAO,
    CONCLUIDA,
    ADIADA,
    CANCELADA,
    ARQUIVADA;

    /** Nome canonico aceito pelo plugin WP (strings acima). */
    fun toApi(): String = name

    companion object {
        /**
         * Converte string da API em enum. Aceita tanto nomes canonicos do WP
         * quanto aliases legados do Android ("PENDENTE" -> PLANEJADA).
         */
        fun fromApi(value: String?): StatusTarefa = when (value?.uppercase()?.trim()) {
            null, "" -> PLANEJADA
            // canonicos WP
            "CAIXA_ENTRADA" -> CAIXA_ENTRADA
            "PLANEJADA" -> PLANEJADA
            "EM_ANDAMENTO" -> EM_ANDAMENTO
            "AGUARDANDO_TERCEIRO" -> AGUARDANDO_TERCEIRO
            "BLOQUEADA" -> BLOQUEADA
            "EM_REVISAO" -> EM_REVISAO
            "ENTREGUE_AGUARDANDO_CONFIRMACAO" -> ENTREGUE_AGUARDANDO_CONFIRMACAO
            "CONCLUIDA" -> CONCLUIDA
            "ADIADA" -> ADIADA
            "CANCELADA" -> CANCELADA
            "ARQUIVADA" -> ARQUIVADA
            // aliases legados Android
            "PENDENTE" -> PLANEJADA
            "EM_PROGRESSO" -> EM_ANDAMENTO
            "EM_REVISÃO" -> EM_REVISAO
            "AGUARDANDO" -> AGUARDANDO_TERCEIRO
            "PARADA" -> BLOQUEADA
            "PENDENTE_APROVACAO" -> EM_REVISAO
            else -> PLANEJADA // default seguro
        }
    }
}
