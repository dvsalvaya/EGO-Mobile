package com.ego.mobile.data.local.mapper

import com.ego.mobile.data.local.entity.HabitEntity
import com.ego.mobile.domain.model.Habit
import com.ego.mobile.domain.model.HabitFrequency
import com.ego.mobile.domain.model.HabitStatus

fun Habit.toEntity(): HabitEntity = HabitEntity(
    id = id,
    name = name,
    description = description,
    frequency = frequency.toPersistedValue(),
    status = status.name,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun HabitEntity.toDomain(): Habit {
    val parsedStatus = try {
        HabitStatus.valueOf(status)
    } catch (_: IllegalArgumentException) {
        HabitStatus.ACTIVE
    }

    return Habit(
        id = id,
        name = name,
        description = description,
        frequency = HabitFrequency.fromPersistedValue(frequency),
        status = parsedStatus,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
