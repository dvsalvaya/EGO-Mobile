package com.ego.mobile.feature.tasks

import com.ego.mobile.domain.model.Task
import java.time.Instant
import java.time.ZoneId

data class TasksUiState(
    val openTasks: List<Task> = emptyList(),
    val completedTasks: List<Task> = emptyList(),
    val showCompletedTasks: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val currentTimeMillis: Long = System.currentTimeMillis(),
) {
    val visibleTasks: List<Task>
        get() = if (showCompletedTasks) completedTasks else openTasks

    val taskGroups: List<TaskGroup>
        get() {
            val zone = PRODUCT_ZONE
            val today = Instant.ofEpochMilli(currentTimeMillis).atZone(zone).toLocalDate()
            val dated = openTasks.filter { it.dueAt != null }
            val overdue = dated.filter { it.isOverdue(currentTimeMillis) }
                .sortedWith(compareBy<Task>({ it.dueAt }, { -it.createdAt }))
            val dueToday = dated.filter {
                !it.isOverdue(currentTimeMillis) &&
                    Instant.ofEpochMilli(it.dueAt!!).atZone(zone).toLocalDate() == today
            }.sortedWith(compareBy<Task>({ it.dueAt }, { -it.createdAt }))
            val upcoming = dated.filter {
                Instant.ofEpochMilli(it.dueAt!!).atZone(zone).toLocalDate().isAfter(today)
            }.sortedWith(compareBy<Task>({ it.dueAt }, { -it.createdAt }))
            val noDeadline = openTasks.filter { it.dueAt == null }.sortedByDescending { it.createdAt }
            return listOf(
                TaskGroup(TaskGroupType.OVERDUE, overdue),
                TaskGroup(TaskGroupType.TODAY, dueToday),
                TaskGroup(TaskGroupType.UPCOMING, upcoming + noDeadline),
            )
        }
}

data class TaskGroup(val type: TaskGroupType, val tasks: List<Task>)

enum class TaskGroupType(val title: String) {
    OVERDUE("Atrasadas"),
    TODAY("Hoje"),
    UPCOMING("Próximas"),
}

private val PRODUCT_ZONE = ZoneId.of("America/Sao_Paulo")

sealed interface TasksEvent {
    data class TaskDeleted(val title: String) : TasksEvent
}
