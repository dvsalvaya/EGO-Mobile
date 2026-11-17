package com.ego.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "training_models_exercises",
    foreignKeys = [
        ForeignKey(
            entity = TrainingModelEntity::class,
            parentColumns = ["id"],
            childColumns = ["modelId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["modelId"]),
        Index(value = ["modelId", "orderIndex"]),
    ],
)
data class TrainingModelExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val modelId: Long,
    val name: String,
    val orderIndex: Int,
    val setsCount: Int,
    val reps: String? = null,
    val weightKg: Double? = null,
    val notes: String? = null,
)
