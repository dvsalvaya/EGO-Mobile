package com.ego.mobile.domain.model

import java.time.LocalDate

/** Consistência dos últimos sete dias locais, do mais antigo até hoje. */
data class WeeklyConsistency(
    val days: List<Day>,
) {
    data class Day(
        val date: LocalDate,
        val count: Int,
        /** Nível de intensidade: 0, 1, 2 ou 3 (três ou mais atividades). */
        val level: Int,
    )

    val levelByCivilDate: Map<String, Int>
        get() = days.associate { it.date.toString() to it.level }

    companion object {
        const val DAY_COUNT = 7

        fun empty(referenceDate: LocalDate): WeeklyConsistency = WeeklyConsistency(
            days = (DAY_COUNT - 1 downTo 0).map { offset ->
                val date = referenceDate.minusDays(offset.toLong())
                Day(date = date, count = 0, level = 0)
            },
        )
    }
}
