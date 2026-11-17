package com.ego.mobile.feature.home

import com.ego.mobile.domain.model.Habit
import com.ego.mobile.domain.model.Task

sealed interface HomeUiState {
    data object Loading : HomeUiState

    data class Content(
        val summary: HomeSummary,
        val todaySummary: TodaySummary,
        val consistencyGrid: ConsistencyGrid,
        val errorMessage: String? = null,
    ) : HomeUiState

    data class Error(val message: String) : HomeUiState

    data class HomeSummary(
        val activeRoutine: RoutineSummary?,
        val todayHabits: List<HabitSummary>,
        val priorityTasks: List<TaskSummary>
    )

    data class HabitSummary(
        val habit: Habit,
        val todayOccurrences: Int,
        val status: HabitStatus
    )

    data class TodaySummary(
        val predictedHabits: Int, // ocorrências previstas para hoje
        val openTasksToday: Int, // tarefas abertas com prazo hoje
        val overdueTasks: Int, // tarefas atrasadas
        val totalTasks: Int // total de tarefas abertas
    )

    data class ConsistencyGrid(
        val dayIntensityMap: Map<String, Int>, // data civil -> nível de intensidade (0-3+)
        val totalDays: Int = 7 // sempre 7 dias incluindo hoje
    )

    data class TaskSummary(
        val task: Task,
        val isOverdue: Boolean,
        val isDueToday: Boolean
    )
}