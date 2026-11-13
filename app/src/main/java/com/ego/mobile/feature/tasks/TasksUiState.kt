package com.ego.mobile.feature.tasks

import com.ego.mobile.domain.model.Task

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
}

sealed interface TasksEvent {
    data class TaskDeleted(val title: String) : TasksEvent
}
