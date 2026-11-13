package com.ego.mobile.domain.model

import java.time.LocalDate

enum class HabitOccurrenceStatus {
    PENDING,
    COMPLETED,
    SKIPPED
}

data class HabitOccurrence(
    val id: Long = 0L,
    val habitId: Long,
    val date: LocalDate,
    val status: HabitOccurrenceStatus = HabitOccurrenceStatus.PENDING,
    val completedAt: Long? = null,
    val notes: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
) {
    init {
        require(notes == null || notes.length <= 250) {
            "Habit occurrence notes must be 250 characters or less."
        }
    }
}