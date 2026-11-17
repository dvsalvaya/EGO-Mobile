package com.ego.mobile.domain.model

import java.time.LocalDate

/**
 * Conclusão real de uma atividade, com identidade canônica e data civil do evento.
 *
 * A chave (sourceType, sourceId) permite deduplicar a mesma atividade quando ela é
 * apresentada em mais de uma tela (Home, lista e Histórico).
 */
data class ConsistencyEvent(
    val sourceType: ActivityType,
    val sourceId: Long,
    val date: LocalDate,
)
