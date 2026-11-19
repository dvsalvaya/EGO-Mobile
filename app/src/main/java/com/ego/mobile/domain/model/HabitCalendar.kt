package com.ego.mobile.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

val habitZone: ZoneId = ZoneId.of("America/Sao_Paulo")

data class HabitPlan(val effectiveFrom: LocalDate, val frequency: HabitFrequency, val status: HabitStatus)

data class HabitDay(
    val date: LocalDate,
    val expected: Boolean,
    val occurrence: HabitOccurrence?,
    val weeklyTarget: Int?,
) {
    val completed: Boolean get() = occurrence?.status == HabitOccurrenceStatus.COMPLETED

    fun missedOn(today: LocalDate): Boolean {
        if (completed || (weeklyTarget != null && !expected)) return false
        return (expected && date < today) || occurrence?.status == HabitOccurrenceStatus.SKIPPED
    }
}

data class HabitCalendar(
    val habit: Habit,
    val plans: List<HabitPlan>,
    val occurrences: List<HabitOccurrence>,
) {
    val firstDate: LocalDate? get() = listOfNotNull(
        plans.minOfOrNull { it.effectiveFrom }, occurrences.minOfOrNull { it.date },
    ).minOrNull()

    /** Includes off-plan weekly dates for factual corrections. Only expected dates enter the grid. */
    fun days(from: LocalDate, through: LocalDate): List<HabitDay> {
        val byDate = occurrences.associateBy { it.date }
        val revisions = plans.sortedBy { it.effectiveFrom }
        var index = 0
        var plan: HabitPlan? = null
        var date = from.with(DayOfWeek.MONDAY)
        var completedInWeek = 0
        return buildList {
            while (date <= through) {
                if (date.dayOfWeek == DayOfWeek.MONDAY) completedInWeek = 0
                while (index < revisions.size && revisions[index].effectiveFrom <= date) {
                    plan = revisions[index++]
                }
                val frequency = plan?.takeIf { it.status == HabitStatus.ACTIVE }?.frequency
                val weekly = frequency as? HabitFrequency.TimesPerWeek
                val expected = when (frequency) {
                    null -> false
                    is HabitFrequency.TimesPerWeek -> completedInWeek < frequency.count && date.dayOfWeek in frequency.preferredDays
                    else -> frequency.requiresOccurrenceOn(date)
                }
                val fact = byDate[date]
                if (date >= from && (weekly != null || expected || fact != null)) {
                    add(HabitDay(date, expected, fact, weekly?.count))
                }
                if (fact?.status == HabitOccurrenceStatus.COMPLETED) completedInWeek++
                date = date.plusDays(1)
            }
        }
    }
}
