package com.ego.mobile.domain.model

import java.time.DayOfWeek
import java.time.LocalDate

sealed class HabitFrequency {
    data object Daily : HabitFrequency() {
        override fun requiresOccurrenceOn(date: LocalDate): Boolean = true
        override fun toPersistedValue(): String = "DAILY"
    }

    data class WeeklyDays(val days: Set<DayOfWeek>) : HabitFrequency() {
        init {
            require(days.isNotEmpty()) { "WeeklyDays frequency requires at least one day." }
        }

        override fun requiresOccurrenceOn(date: LocalDate): Boolean = date.dayOfWeek in days

        override fun toPersistedValue(): String {
            val ordering = days.sortedBy { it.value }
            return "WEEKLY_DAYS:${ordering.joinToString(",") { it.name }}"
        }
    }

    data class TimesPerWeek(val count: Int) : HabitFrequency() {
        init {
            require(count in 1..7) { "Times per week must be between 1 and 7." }
        }

        // Qualquer dia da semana é elegível; o limite semanal é avaliado por
        // HabitProgressRules a partir das conclusões reais da semana ISO.
        override fun requiresOccurrenceOn(date: LocalDate): Boolean = true

        override fun toPersistedValue(): String = "TIMES_PER_WEEK:$count"
    }

    abstract fun requiresOccurrenceOn(date: LocalDate): Boolean
    abstract fun toPersistedValue(): String

    companion object {
        fun fromPersistedValue(value: String): HabitFrequency {
            return when {
                value.equals("DAILY", ignoreCase = true) -> Daily
                value.startsWith("WEEKLY_DAYS:", ignoreCase = true) -> {
                    val days = value.removePrefix("WEEKLY_DAYS:")
                        .split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                        .mapNotNull { dayName ->
                            runCatching { DayOfWeek.valueOf(dayName) }.getOrNull()
                        }
                        .toSet()

                    if (days.isEmpty()) Daily else WeeklyDays(days)
                }
                value.startsWith("TIMES_PER_WEEK:", ignoreCase = true) -> {
                    val count = value.removePrefix("TIMES_PER_WEEK:")
                        .toIntOrNull()
                        ?.coerceIn(1, 7)
                        ?: 1
                    TimesPerWeek(count)
                }
                else -> Daily
            }
        }
    }
}