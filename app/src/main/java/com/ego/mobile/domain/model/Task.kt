package com.ego.mobile.domain.model

import java.time.Instant
import java.time.ZoneId

data class Task(
    val id: Long = 0L,
    val title: String,
    val description: String? = null,
    val dueAt: Long? = null,
    val status: TaskStatus = TaskStatus.OPEN,
    val createdAt: Long,
    val updatedAt: Long,
    val completedAt: Long? = null,
) {
    init {
        require(title.isNotBlank()) { "Task title is required." }
        require(title.trim().length in 1..100) { "Task title must be 1 to 100 characters." }
        require(description == null || description.length <= 300) {
            "Task description must be 300 characters or less."
        }
    }

    fun isOverdue(nowMillis: Long = System.currentTimeMillis()): Boolean {
        if (status != TaskStatus.OPEN || dueAt == null) return false
        val zone = ZoneId.of("America/Sao_Paulo")
        val dueDate = Instant.ofEpochMilli(dueAt).atZone(zone).toLocalDate()
        val today = Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
        return dueDate.isBefore(today)
    }
}
