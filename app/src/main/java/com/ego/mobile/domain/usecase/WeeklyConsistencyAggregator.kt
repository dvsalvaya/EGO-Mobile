package com.ego.mobile.domain.usecase

import com.ego.mobile.domain.model.ActivityType
import com.ego.mobile.domain.model.ConsistencyEvent
import com.ego.mobile.domain.model.WeeklyConsistency
import java.time.LocalDate

/** Agregação pura dos eventos canônicos em sete dias locais. */
internal fun aggregateWeeklyConsistency(
    events: Collection<ConsistencyEvent>,
    today: LocalDate,
): WeeklyConsistency {
    val lastDay = today.minusDays((WeeklyConsistency.DAY_COUNT - 1).toLong())
    val counts = events.asSequence()
        .filter { it.sourceType != ActivityType.HAIRCARE }
        .distinctBy { it.sourceType to it.sourceId }
        .filter { it.date in lastDay..today }
        .groupingBy { it.date }
        .eachCount()

    return WeeklyConsistency(
        days = (WeeklyConsistency.DAY_COUNT - 1 downTo 0).map { offset ->
            val date = today.minusDays(offset.toLong())
            val count = counts[date] ?: 0
            WeeklyConsistency.Day(
                date = date,
                count = count,
                level = levelOf(count),
            )
        },
    )
}

private fun levelOf(count: Int): Int = when {
    count <= 0 -> 0
    count == 1 -> 1
    count == 2 -> 2
    else -> 3
}
