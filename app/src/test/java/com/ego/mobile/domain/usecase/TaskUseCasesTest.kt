package com.ego.mobile.domain.usecase

import com.ego.mobile.domain.model.Task
import com.ego.mobile.domain.model.TaskStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class TaskUseCasesTest {

    @Test
    fun rescheduleTaskUpdatesDueAtAndClearsLateState() {
        val now = System.currentTimeMillis()
        val task = Task(
            title = "Tarefa vencida",
            dueAt = now - 30_000L,
            status = TaskStatus.OPEN,
            createdAt = now - 60_000L,
            updatedAt = now - 60_000L,
        )

        val newDueAt = now + 86_400_000L
        val updated = RescheduleTaskUseCase().invoke(task, newDueAt)

        assertEquals(newDueAt, updated.dueAt)
        assertFalse(updated.isOverdue(now))
    }

    @Test
    fun deleteTaskWithUndoKeepsRestorableSnapshot() {
        val now = System.currentTimeMillis()
        val task = Task(
            title = "Excluir depois",
            dueAt = now + 60_000L,
            status = TaskStatus.OPEN,
            createdAt = now - 10_000L,
            updatedAt = now - 10_000L,
        )

        val result = DeleteTaskWithUndoUseCase().invoke(task)

        assertEquals(task.id, result.deletedTask.id)
        assertTrue(result.canUndo)
        assertEquals(task.title, result.deletedTask.title)
    }
}
