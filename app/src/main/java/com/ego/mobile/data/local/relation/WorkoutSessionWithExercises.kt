package com.ego.mobile.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.ego.mobile.data.local.entity.WorkoutExerciseEntity
import com.ego.mobile.data.local.entity.WorkoutSessionEntity

data class WorkoutSessionWithExercises(
    @Embedded
    val session: WorkoutSessionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "sessionId",
    )
    val exercises: List<WorkoutExerciseEntity> = emptyList(),
)
