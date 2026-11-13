package com.ego.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "haircare_occurrences",
    foreignKeys = [
        ForeignKey(
            entity = HaircareRoutineEntity::class,
            parentColumns = ["id"],
            childColumns = ["routineId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["routineId"]),
        Index(value = ["scheduledDate"]),
        Index(value = ["status"]),
    ],
)
data class HaircareOccurrenceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val routineId: Long,
    val stepType: String,
    val stepTitle: String,
    val scheduledDate: String,
    val executedAt: Long? = null,
    val status: String, // 'PENDING', 'COMPLETED', 'RESCHEDULED', 'SKIPPED'
    val notes: String? = null,
    val orderIndex: Int = 0,
)
