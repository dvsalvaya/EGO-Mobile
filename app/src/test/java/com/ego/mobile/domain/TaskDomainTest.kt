package com.ego.mobile.domain

import com.ego.mobile.domain.model.Task
import com.ego.mobile.domain.model.TaskStatus
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskDomainTest {

    @Test
    fun titleIsRequired() {
        val ex = org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
            Task(
                title = "   ",
                description = null,
                dueAt = null,
                status = TaskStatus.OPEN,
                createdAt = 1_000L,
                updatedAt = 1_000L,
            )
        }

        assertTrue(ex.message.orEmpty().contains("title", ignoreCase = true))
    }

    @Test
    fun overdueRulesMatchStatusAndDueDate() {
        val now = System.currentTimeMillis()

        val overdueOpenTask = Task(
            title = "Enviar proposta",
            dueAt = now - 10_000L,
            status = TaskStatus.OPEN,
            createdAt = now - 50_000L,
            updatedAt = now - 50_000L,
        )
        assertTrue(overdueOpenTask.isOverdue(now))

        val noDueTask = Task(
            title = "Leitura leve",
            dueAt = null,
            status = TaskStatus.OPEN,
            createdAt = now - 50_000L,
            updatedAt = now - 50_000L,
        )
        assertFalse(noDueTask.isOverdue(now))

        val completedTask = overdueOpenTask.copy(
            status = TaskStatus.COMPLETED,
            completedAt = now - 5_000L,
            updatedAt = now - 5_000L,
        )
        assertFalse(completedTask.isOverdue(now))
    }
}
