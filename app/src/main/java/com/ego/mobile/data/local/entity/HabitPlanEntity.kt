package com.ego.mobile.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Relation

@Entity(
    tableName = "habit_plans",
    primaryKeys = ["habitId", "effectiveFrom"],
    foreignKeys = [ForeignKey(
        entity = HabitEntity::class, parentColumns = ["id"], childColumns = ["habitId"],
        onDelete = ForeignKey.CASCADE,
    )],
)
data class HabitPlanEntity(
    val habitId: Long,
    val effectiveFrom: String,
    val frequency: String,
    val status: String,
)

data class HabitCalendarEntities(
    @Embedded val habit: HabitEntity,
    @Relation(parentColumn = "id", entityColumn = "habitId") val plans: List<HabitPlanEntity>,
    @Relation(parentColumn = "id", entityColumn = "habitId") val occurrences: List<HabitOccurrenceEntity>,
)
