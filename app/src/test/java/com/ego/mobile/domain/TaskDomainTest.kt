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
        val now = epoch(2026, 11, 16, 12, 0)
        val zone = java.time.ZoneId.of("America/Sao_Paulo")

        val overdueOpenTask = Task(
            title = "Enviar proposta",
            dueAt = epoch(2026, 11, 15, 0, 0),
            status = TaskStatus.OPEN,
            createdAt = now - 50_000L,
            updatedAt = now - 50_000L,
        )
        assertTrue(overdueOpenTask.isOverdue(now, zone))

        val dueToday = overdueOpenTask.copy(dueAt = epoch(2026, 11, 16, 0, 0))
        assertFalse(dueToday.isOverdue(now, zone))
        assertFalse(dueToday.isOverdue(epoch(2026, 11, 16, 23, 59), zone))
        assertTrue(dueToday.isOverdue(epoch(2026, 11, 17, 0, 1), zone))

        val dueTomorrow = overdueOpenTask.copy(dueAt = epoch(2026, 11, 17, 0, 0))
        assertFalse(dueTomorrow.isOverdue(now, zone))

        val noDueTask = Task(
            title = "Leitura leve",
            dueAt = null,
            status = TaskStatus.OPEN,
            createdAt = now - 50_000L,
            updatedAt = now - 50_000L,
        )
        assertFalse(noDueTask.isOverdue(now, zone))

        val completedTask = overdueOpenTask.copy(
            status = TaskStatus.COMPLETED,
            completedAt = now - 5_000L,
            updatedAt = now - 5_000L,
        )
        assertFalse(completedTask.isOverdue(now, zone))
    }

    private fun epoch(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long =
        java.time.ZonedDateTime.of(
            java.time.LocalDateTime.of(year, month, day, hour, minute),
            java.time.ZoneId.of("America/Sao_Paulo"),
        ).toInstant().toEpochMilli()
}
