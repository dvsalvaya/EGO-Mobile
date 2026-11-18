package com.ego.mobile.domain

import com.ego.mobile.domain.model.Task
import com.ego.mobile.domain.model.TaskStatus
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

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
    fun titleAndDescriptionRespectTheirMaximumLengths() {
        org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
            Task(
                title = "T".repeat(101),
                createdAt = 1_000L,
                updatedAt = 1_000L,
            )
        }
        org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
            Task(
                title = "Válida",
                description = "D".repeat(301),
                createdAt = 1_000L,
                updatedAt = 1_000L,
            )
        }
    }

    @Test
    fun overdueRulesMatchStatusAndDueDate() {
        val now = System.currentTimeMillis()

        val overdueOpenTask = Task(
            title = "Enviar proposta",
            dueAt = java.time.Instant.ofEpochMilli(now)
                .atZone(java.time.ZoneId.of("America/Sao_Paulo"))
                .toLocalDate().minusDays(1)
                .atStartOfDay(java.time.ZoneId.of("America/Sao_Paulo")).toInstant().toEpochMilli(),
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

    @Test
    fun `due date is compared as civil date in Sao Paulo`() {
        val zone = ZoneId.of("America/Sao_Paulo")
        val today = LocalDate.of(2026, 11, 18)
        val now = today.atTime(23, 59).atZone(zone).toInstant().toEpochMilli()
        fun taskFor(date: LocalDate, status: TaskStatus = TaskStatus.OPEN, due: Boolean = true) = Task(
            title = "Tarefa",
            dueAt = if (due) date.atStartOfDay(zone).toInstant().toEpochMilli() else null,
            status = status,
            createdAt = now - 1000,
            updatedAt = now - 1000,
        )

        assertTrue(taskFor(today.minusDays(1)).isOverdue(now))
        assertFalse(taskFor(today).isOverdue(now))
        assertFalse(taskFor(today.plusDays(1)).isOverdue(now))
        assertTrue(taskFor(today).isOverdue(today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()))
        assertFalse(taskFor(today.minusDays(1), TaskStatus.COMPLETED).isOverdue(now))
        assertFalse(taskFor(today, due = false).isOverdue(now))
    }
}
