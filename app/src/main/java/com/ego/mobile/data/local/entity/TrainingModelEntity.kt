package com.ego.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "training_models",
    indices = [Index(value = ["name"])],
)
data class TrainingModelEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val createdAt: Long,
    val updatedAt: Long,
)
