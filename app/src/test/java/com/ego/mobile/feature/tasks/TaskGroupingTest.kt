package com.ego.mobile.feature.tasks

import com.ego.mobile.domain.model.Task
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class TaskGroupingTest {
    private val zone = ZoneId.of("America/Sao_Paulo")
    private val now = LocalDate.of(2026, 11, 18).atTime(12, 0).atZone(zone).toInstant().toEpochMilli()

    @Test
    fun openTasksAreGroupedOnceByCivilDueDateAndUndatedTasksComeLast() {
        val tasks = listOf(
            task(1, "Ontem", date(17)),
            task(2, "Hoje", date(18)),
            task(3, "Amanhã", date(19)),
            task(4, "Sem prazo", null),
        )
        val groups = TasksUiState(openTasks = tasks, currentTimeMillis = now).taskGroups
        assertEquals(listOf("Ontem"), groups[0].tasks.map { it.title })
        assertEquals(listOf("Hoje"), groups[1].tasks.map { it.title })
        assertEquals(listOf("Amanhã", "Sem prazo"), groups[2].tasks.map { it.title })
        assertEquals(4, groups.flatMap { it.tasks }.map { it.id }.toSet().size)
    }

    @Test
    fun groupsAreOrderedByOldestDeadlineAndDueTodayRemainsTodayUntilNextCivilDay() {
        val midnight = LocalDate.of(2026, 11, 18).atStartOfDay(zone).toInstant().toEpochMilli()
        val tasks = listOf(
            task(1, "Hoje tarde", date(18, 18)),
            task(2, "Hoje cedo", date(18, 8)),
            task(3, "Venceu antes", date(17, 18)),
            task(4, "Vence hoje", midnight),
        )
        val groups = TasksUiState(openTasks = tasks, currentTimeMillis = midnight).taskGroups
        assertEquals(listOf("Venceu antes"), groups[0].tasks.map { it.title })
        assertEquals(listOf("Vence hoje", "Hoje cedo", "Hoje tarde"), groups[1].tasks.map { it.title })
    }

    private fun date(day: Int, hour: Int = 0) =
        LocalDate.of(2026, 11, day).atTime(hour, 0).atZone(zone).toInstant().toEpochMilli()

    private fun task(id: Long, title: String, dueAt: Long?) = Task(
        id = id, title = title, dueAt = dueAt, createdAt = id, updatedAt = id,
    )
}
