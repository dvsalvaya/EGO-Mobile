package com.ego.mobile.data.local.mapper

import com.ego.mobile.data.local.entity.TaskEntity
import com.ego.mobile.domain.model.Task
import com.ego.mobile.domain.model.TaskStatus

fun Task.toEntity(): TaskEntity = TaskEntity(
    id = id,
    title = title,
    description = description,
    dueAt = dueAt,
    status = status.name,
    createdAt = createdAt,
    updatedAt = updatedAt,
    completedAt = completedAt,
)

fun TaskEntity.toDomain(): Task = Task(
    id = id,
    title = title,
    description = description,
    dueAt = dueAt,
    status = TaskStatus.valueOf(status),
    createdAt = createdAt,
    updatedAt = updatedAt,
    completedAt = completedAt,
)
