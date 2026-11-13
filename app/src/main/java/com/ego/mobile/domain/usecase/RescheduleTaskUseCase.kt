package com.ego.mobile.domain.usecase

import com.ego.mobile.domain.model.Task

class RescheduleTaskUseCase {
    operator fun invoke(task: Task, newDueAt: Long): Task = task.copy(
        dueAt = newDueAt,
        updatedAt = System.currentTimeMillis(),
    )
}
