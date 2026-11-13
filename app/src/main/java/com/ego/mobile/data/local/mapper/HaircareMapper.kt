package com.ego.mobile.data.local.mapper

import com.ego.mobile.data.local.entity.HaircareOccurrenceEntity
import com.ego.mobile.data.local.entity.HaircareRoutineEntity
import com.ego.mobile.domain.model.HaircareOccurrence
import com.ego.mobile.domain.model.HaircareOccurrenceStatus
import com.ego.mobile.domain.model.HaircareRoutine
import com.ego.mobile.domain.model.HaircareRoutineSource
import com.ego.mobile.domain.model.HaircareStepType

fun HaircareRoutineEntity.toDomain(): HaircareRoutine = HaircareRoutine(
    id = id,
    name = name,
    source = HaircareRoutineSource.valueOf(source),
    description = description,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun HaircareRoutine.toEntity(): HaircareRoutineEntity = HaircareRoutineEntity(
    id = id,
    name = name,
    source = source.name,
    description = description,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun HaircareOccurrenceEntity.toDomain(): HaircareOccurrence = HaircareOccurrence(
    id = id,
    routineId = routineId,
    stepType = HaircareStepType.valueOf(stepType),
    stepTitle = stepTitle,
    scheduledDate = scheduledDate,
    executedAt = executedAt,
    status = HaircareOccurrenceStatus.valueOf(status),
    notes = notes,
    orderIndex = orderIndex,
)

fun HaircareOccurrence.toEntity(): HaircareOccurrenceEntity = HaircareOccurrenceEntity(
    id = id,
    routineId = routineId,
    stepType = stepType.name,
    stepTitle = stepTitle,
    scheduledDate = scheduledDate,
    executedAt = executedAt,
    status = status.name,
    notes = notes,
    orderIndex = orderIndex,
)
