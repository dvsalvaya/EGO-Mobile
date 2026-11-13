package com.ego.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "habit_occurrences",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index(value = ["habitId", "dateCivil"], unique = true)],
)
data class HabitOccurrenceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val habitId: Long,
    val dateCivil: String,
    val status: String,
    val completedAt: Long? = null,
    val notes: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
)