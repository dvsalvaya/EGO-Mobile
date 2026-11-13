package com.ego.mobile.data.local.mapper

import com.ego.mobile.data.local.entity.HabitOccurrenceEntity
import com.ego.mobile.domain.model.HabitOccurrence
import com.ego.mobile.domain.model.HabitOccurrenceStatus
import java.time.LocalDate

fun HabitOccurrence.toEntity(): HabitOccurrenceEntity = HabitOccurrenceEntity(
    id = id,
    habitId = habitId,
    dateCivil = date.toString(),
    status = status.name,
    completedAt = completedAt,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun HabitOccurrenceEntity.toDomain(): HabitOccurrence {
    val parsedStatus = try {
        HabitOccurrenceStatus.valueOf(status)
    } catch (_: IllegalArgumentException) {
        HabitOccurrenceStatus.PENDING
    }

    return HabitOccurrence(
        id = id,
        habitId = habitId,
        date = LocalDate.parse(dateCivil),
        status = parsedStatus,
        completedAt = completedAt,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}