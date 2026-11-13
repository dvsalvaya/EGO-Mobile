package com.ego.mobile.domain.usecase

import com.ego.mobile.domain.model.Task

class RescheduleTaskUseCase {
    operator fun invoke(task: Task, newDueAt: Long): Task {
        require(newDueAt > 0L) { "Task due date must be a positive timestamp." }
        return task.copy(
            dueAt = newDueAt,
            updatedAt = System.currentTimeMillis(),
        )
    }
}
