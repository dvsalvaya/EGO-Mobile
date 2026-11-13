package com.ego.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "haircare_routines")
data class HaircareRoutineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val source: String, // 'PRESET', 'CUSTOM'
    val description: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
)
