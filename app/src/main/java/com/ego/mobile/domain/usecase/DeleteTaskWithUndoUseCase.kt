package com.ego.mobile.domain.usecase

import com.ego.mobile.domain.model.Task

class DeleteTaskWithUndoUseCase {
    data class Result(
        val deletedTask: Task,
        val canUndo: Boolean,
    )

    operator fun invoke(task: Task): Result = Result(
        deletedTask = task,
        canUndo = true,
    )
}
