package com.ego.mobile.data.local.mapper

import com.ego.mobile.data.local.entity.TrainingModelEntity
import com.ego.mobile.data.local.entity.TrainingModelExerciseEntity
import com.ego.mobile.domain.model.WorkoutTemplate
import com.ego.mobile.domain.model.WorkoutTemplateExercise

fun TrainingModelEntity.toDomain(exercises: List<TrainingModelExerciseEntity> = emptyList()): WorkoutTemplate =
    WorkoutTemplate(
        id = id,
        name = name,
        exercises = exercises.map { it.toDomain() },
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

fun TrainingModelExerciseEntity.toDomain(): WorkoutTemplateExercise =
    WorkoutTemplateExercise(
        id = id,
        templateId = modelId,
        name = name,
        orderIndex = orderIndex,
        setsCount = setsCount,
        reps = reps,
        weightKg = weightKg,
        notes = notes,
    )

fun WorkoutTemplate.toEntity(): TrainingModelEntity =
    TrainingModelEntity(
        id = id,
        name = name.trim(),
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

fun WorkoutTemplateExercise.toEntity(): TrainingModelExerciseEntity =
    TrainingModelExerciseEntity(
        id = id,
        modelId = templateId,
        name = name.trim(),
        orderIndex = orderIndex,
        setsCount = setsCount,
        reps = reps?.trim()?.ifBlank { null },
        weightKg = weightKg,
        notes = notes?.trim()?.ifBlank { null },
    )
