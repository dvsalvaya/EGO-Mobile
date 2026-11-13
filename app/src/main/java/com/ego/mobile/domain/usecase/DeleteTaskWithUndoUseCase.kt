package com.ego.mobile.domain.usecase

import com.ego.mobile.domain.model.Task
import com.ego.mobile.domain.repository.TaskRepository

class DeleteTaskWithUndoUseCase(
    private val taskRepository: TaskRepository,
    private val undoWindowMillis: Long = DEFAULT_UNDO_WINDOW_MILLIS,
    private val currentTimeMillis: () -> Long = System::currentTimeMillis,
) {
    data class Deletion(
        val deletedTask: Task,
        val canUndo: Boolean,
    )

    private data class PendingDeletion(
        val task: Task,
        val expiresAtMillis: Long,
    )

    private var pendingDeletion: PendingDeletion? = null

    init {
        require(undoWindowMillis > 0L) { "Undo window must be positive." }
    }

    suspend operator fun invoke(taskId: Long): kotlin.Result<Deletion> {
        val deletionResult = taskRepository.deleteTask(taskId)
        if (deletionResult.isFailure) {
            return kotlin.Result.failure(checkNotNull(deletionResult.exceptionOrNull()))
        }

        val task = deletionResult.getOrThrow()
        val expiresAtMillis = currentTimeMillis() + undoWindowMillis
        pendingDeletion = PendingDeletion(task, expiresAtMillis)
        return kotlin.Result.success(Deletion(task, canUndo = true))
    }

    suspend fun undo(): kotlin.Result<Unit> {
        val pending = pendingDeletion
            ?: return kotlin.Result.failure(IllegalStateException("There is no task deletion to undo."))

        if (currentTimeMillis() >= pending.expiresAtMillis) {
            pendingDeletion = null
            return kotlin.Result.failure(IllegalStateException("The undo period has expired."))
        }

        val result = taskRepository.restoreTask(pending.task)
        if (result.isSuccess) {
            pendingDeletion = null
        }
        return result
    }

    fun discardPendingUndo() {
        pendingDeletion = null
    }

    private companion object {
        const val DEFAULT_UNDO_WINDOW_MILLIS = 5_000L
    }
}
