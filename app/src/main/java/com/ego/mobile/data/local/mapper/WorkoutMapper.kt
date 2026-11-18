package com.ego.mobile.data.local.mapper

import com.ego.mobile.data.local.entity.WorkoutExerciseEntity
import com.ego.mobile.data.local.entity.WorkoutSessionEntity
import com.ego.mobile.data.local.relation.WorkoutSessionWithExercises
import com.ego.mobile.domain.model.WorkoutExercise
import com.ego.mobile.domain.model.WorkoutSession
import com.ego.mobile.domain.model.WorkoutSessionStatus

fun WorkoutExerciseEntity.toDomain(): WorkoutExercise {
    return WorkoutExercise(
        id = id,
        sessionId = sessionId,
        name = name,
        orderIndex = orderIndex,
        setsCount = setsCount,
        reps = reps,
        weightKg = weightKg,
        notes = notes,
    )
}

fun WorkoutExercise.toEntity(): WorkoutExerciseEntity {
    return WorkoutExerciseEntity(
        id = id,
        sessionId = sessionId,
        name = name,
        orderIndex = orderIndex,
        setsCount = setsCount,
        reps = reps,
        weightKg = weightKg,
        notes = notes,
    )
}

fun WorkoutSessionEntity.toDomain(exercises: List<WorkoutExerciseEntity> = emptyList()): WorkoutSession {
    return WorkoutSession(
        id = id,
        title = title,
        startedAt = startedAt,
        endedAt = endedAt,
        status = runCatching { WorkoutSessionStatus.valueOf(status) }.getOrDefault(WorkoutSessionStatus.IN_PROGRESS),
        notes = notes,
        exercises = exercises.map { it.toDomain() }.sortedBy { it.orderIndex },
        pausedAt = pausedAt,
        pausedDurationMillis = pausedDurationMillis,
    )
}

fun WorkoutSession.toEntity(): WorkoutSessionEntity {
    return WorkoutSessionEntity(
        id = id,
        title = title,
        startedAt = startedAt,
        endedAt = endedAt,
        status = status.name,
        notes = notes,
        pausedAt = pausedAt,
        pausedDurationMillis = pausedDurationMillis,
    )
}

fun WorkoutSessionWithExercises.toDomain(): WorkoutSession {
    return session.toDomain(exercises)
}
